import TSim.*;
import java.util.concurrent.locks;
import java.util.HashMap;

   /** 
    * @author Andreas Hagesjö och Robert Nyquist
    */
public class Lab2 {
   private int simulationspeed = 100;
   private final int MAXSPEED = 19;
   private final int down = 1; 
   private final int up = -1;
   Monitor monitor0 = new Monitor();
   Monitor monitor1 = new Monitor();
   Monitor monitor2 = new Monitor();
   Monitor monitor3 = new Monitor();
   Monitor monitor4 = new Monitor();
   Monitor monitor5 = new Monitor();



   public static void main(String[] args) throws InterruptedException, CommandException{
      new Lab2(args);
   }

   public Lab2(String[] args) throws InterruptedException, CommandException {

      TSimInterface tsi = TSimInterface.getInstance();
      tsi.setDebug(true);
      Train train1 = new Train(1, down); 
      Train train2 = new Train(2, up);
      
      if (args.length >= 1) {
         simulationspeed = Integer.parseInt(args[args.length - 1]);
      }
      if (args.length == 2) {
         train1.initspeed = Integer.parseInt(args[0]);
      }
      if (args.length == 3) {
         train1.initspeed = Integer.parseInt(args[0]);
         train2.initspeed = Integer.parseInt(args[1]);
      }

      train1.setSpeed(train1.initspeed);
      train2.setSpeed(train2.initspeed);
      train1.start();
      train2.start();
   }

   public class Train extends Thread {
      private int direction;
      private int initspeed = (int) (Math.random()*(MAXSPEED - 1) + 1); 
      public int speed;
      public int id;
      public TSimInterface tsi;
      boolean turning = true;


      public Train(int id, int direction) throws InterruptedException, CommandException{
         this.id = id;
         this.direction = direction;
         tsi = TSimInterface.getInstance();
            if (direction == down) {
               monitor3.enter();
            } else {
               monitor1.enter();
            }
      }
      

      public void setSpeed(int speed) throws CommandException{
         this.speed = speed;
         tsi.setSpeed(id,speed);
      }
  

      private void turnTrain() throws CommandException, InterruptedException {
         int tmpspeed = speed;
         setSpeed(0);
         sleep(2000 + 2 * simulationspeed * Math.abs(tmpspeed));
         setSpeed(-tmpspeed);
         initspeed = -tmpspeed;
         direction = -direction;
         turning = false;
      }

      private void waitUntilReady(Monitor track,int x, int y, int switchDirection) throws InterruptedException, CommandException {
	    
            setSpeed(0);
            while(!track.tryEnter()){
		}
            setSpeed(initspeed);
            tsi.setSwitch(x,y, switchDirection);
      }

      @Override
      public void run() {
            //We use 6 semaphores for the different areas.
            //semaphores[0] is the cross at the top
            //semaphores[1] is the bottom station
            //semaphores[2] is the critical "onelinepath" after the bottom station 
            //semaphores[3] is the top station
            //semaphores[4] is the fastest path in the "choose-section" in the middle
            //semaphores[5] is the critical "onelinepath" after the top station

            //Everytime the train has completely left a path connected to a semaphore, this semaphore is released, and the new semaphore (the path the train entered) is (if possible) acquired.
            //Everytime the path (semaphore) the train is about to enter already is acquired, the train stops and waits for the semaphore to be released before starting again (exception below):
            
            //We use tryAcquire everytime the train has to choose path,
            //which means when the train is about to exit a critical path, and therefore doesn't have to stop, it just has to choose path.
            //If the semaphore for the default path fails to be acquired, the train (well...the switch) simply chooses the other free path
         while (true) {
            try {
               SensorEvent sens = tsi.getSensor(id);
               if (sens.getStatus() == 1) {
                  //All cases are sorted according to the map (easier reading)
                  switch (sens.getXpos() * 100 + sens.getYpos()) { //Makes a unique ID for every sensor
                     case 606: case 905:
                        if (direction == down) {
                           setSpeed(0);
			   while(!monitor0.tryEnter())
                           setSpeed(initspeed);
                        } else {
			   monitor0.leave();
                        }
                     break;

                     case 1107: case 1008:
                        if (direction == up) {
                           setSpeed(0);
			   while(!monitor0.tryEnter()){
				}
                           setSpeed(initspeed);
                        } else {
			   monitor5.leave();
                        }
                     break;

                     case 1508:
                        if (direction == down) {
                           waitUntilReady(monitor5,17,7,1);
                        } else {
			   monitor5.leave();
                        }
                        break;

                     case 1407:
                        if (direction == down) {
                           waitUntilReady(monitor5,17,7,2);
			   monitor3.leave();
                        } else {
			   monitor5.leave();
                        }
                        break;

                     case 1908:
                      if (direction == up) {
                           if (monitor3.tryEnter()) {
                              tsi.setSwitch(17,7,2);
                           } else {
                              tsi.setSwitch(17,7,1);
                           }
                        }
                        break;
                        
                     case 1709:
                        if (direction == down) {
                           if (monitor4.tryEnter()) {
                              tsi.setSwitch(15,9,2);
                           } else {
                              tsi.setSwitch(15,9,1);
                           }
                        }
                        break;

                     case 1409:
                        if (direction == up) {
			   monitor4.leave();
                        }
                     break;

                     case 1209:
                        if (direction == up) {
                           waitUntilReady(monitor5,15,9,2);
                        } else {
			   monitor5.leave();
                        }
                        break;

                     case 1310:
                        if (direction == up) {
                           waitUntilReady(monitor5,15,9,1);
                        } else {
			   monitor5.leave();
                        }
                        break;

                     case 709:
                        if (direction == down) {
                           waitUntilReady(monitor2,4,9,1);
                        } else {
			   monitor2.leave();
                        }
                        break;

                     case 610:
                        if (direction == down) {
                           waitUntilReady(monitor2,4,9,2);
                        } else {
			   monitor2.leave();
                        }
                        break;

                     case 509:
                        if (direction == down) {
			   monitor4.leave();
                        }
                     break;

                     case 209:
                    if (direction == up) {
                           if (monitor4.tryEnter()) {
                              tsi.setSwitch(4,9,1);
                           } else {
                              tsi.setSwitch(4,9,2);
                           }
                        } 
                        break;

                     case 110:
                        if (direction == down) {
                             if (monitor1.tryEnter()) {
                              tsi.setSwitch(3,11,1);
                           } else {
                              tsi.setSwitch(3,11,2);
                           }
			}
                           break;

                     case 413:
                        if(direction == up){
                           waitUntilReady(monitor2,3,11,2);
                        } else {
                           monitor2.leave();
                        }
                        break;

                     case 611:
                        if(direction == up){
                           waitUntilReady(monitor2,3,11,1);
                           monitor1.leave();
                        } else {
			   monitor2.leave();
                        }
                        break;

                        //Endstation-sensors. Makes the train reverse
                     case 1411: case 1413: case 1403: case 1405:
                        if (!turning) {
                           turnTrain();
                        } else {
                           turning = false;
                        }
                        break;
                  }
               }
            }
            catch (CommandException e) {
               System.exit(1);
            }
            catch (InterruptedException e) {
               System.exit(1);
            }
         }
      }
   }
 public class Monitor{
	private Lock lock = new ReentrantLock();
	private Condition critical = lock.newCondition();
	private boolean empty = true;

 	public void enter(){
		lock.lock();
		critical.await();
		empty = false;
		lock.unlock();
  	}
	public void leave(){
	lock.lock();
	critical.signal();
	empty = true;
	lock.unlock();
 	}
	public boolean tryEnter(){
		boolean enterd = false;
		if(empty){
			enter();
			enterd = true;
			return enterd;		
		}
		else {
			return enterd;
		}
	}
	}
}

