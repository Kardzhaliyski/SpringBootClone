package events.classes;


import com.github.kardzhaliyski.springbootclone.context.events.ApplicationEvent;

public class MyApplicationEvent extends ApplicationEvent {
    public MyApplicationEvent(Object source) {
        super(source);
    }
}
