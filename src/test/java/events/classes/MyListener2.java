package events.classes;


import com.github.kardzhaliyski.container.events.EventListener;

public class MyListener2 {
    public Object receivedObject = null;
    @EventListener
    public void event(Object obj) {
        receivedObject = obj;
    }
}
