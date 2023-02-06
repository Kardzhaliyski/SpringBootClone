package events.classes;


import com.github.kardzhaliyski.springbootclone.context.events.EventListener;

public class MyListener {
    public Object receivedObject = null;
    @EventListener
    public void event(Object obj) {
        receivedObject = obj;
    }
}
