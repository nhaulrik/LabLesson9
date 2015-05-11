import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.robotics.subsumption.Behavior;

class DetectWall extends Thread implements Behavior
{
  private boolean _suppressed = false;
  private boolean active = false;
  private int distance = 255;

  public DetectWall()
  {
    touch = new TouchSensor(SensorPort.S1);
    sonar = new UltrasonicSensor(SensorPort.S3);
    this.setDaemon(true);
    this.start();
  }
  
  public void run()
  {
    while ( true ) distance = sonar.getDistance();
  }

  public int takeControl()
  {
    if (touch.isPressed() || distance < 25)
       return 100;
    if ( active )
       return 50;
    return 0;
  }

  public void suppress()
  {
    _suppressed = true;// standard practice for suppress methods  
  }

  public void action()
  {
    _suppressed = false;
    active = true;
    Sound.beepSequenceUp();
	
    // Backward for 1000 msec
    LCD.drawString("Drive backward",0,3);
    Motor.A.backward();
    Motor.C.backward();
    int now = (int)System.currentTimeMillis();
    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
    {
       Thread.yield(); //don't exit till suppressed
    }
    
    // Stop for 1000 msec
    LCD.drawString("Stopped       ",0,3);
    Motor.A.stop(); 
    Motor.C.stop();
    now = (int)System.currentTimeMillis();
    while (!_suppressed && ((int)System.currentTimeMillis()< now + 1000) )
    {
      Thread.yield(); //don't exit till suppressed
    }
    
    // Turn
    LCD.drawString("Turn          ",0,3);
    Motor.A.rotate(-180, true);// start Motor.A rotating backward
    Motor.C.rotate(-360, true);  // rotate C farther to make the turn
    while (!_suppressed && Motor.C.isMoving())
    {
      Thread.yield(); //don't exit till suppressed
    }
    Motor.A.stop(); 
    Motor.C.stop();
    LCD.drawString("Stopped       ",0,3);
    Sound.beepSequence();
    active = false;
    
  }
  private TouchSensor touch;
  private UltrasonicSensor sonar;
}
