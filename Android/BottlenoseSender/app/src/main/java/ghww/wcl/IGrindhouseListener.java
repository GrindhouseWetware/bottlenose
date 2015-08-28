package ghww.wcl;

import java.util.Queue;

public interface IGrindhouseListener {
    public void dataChanged(Queue<String> msgQueue);
}
