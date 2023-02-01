package events.classes;


import com.github.kardzhaliyski.container.events.EventListener;

public class MyStringListener {
    public Object receivedObject = null;
    @EventListener
    public void event(String obj) {
        receivedObject = obj;
    }
}
