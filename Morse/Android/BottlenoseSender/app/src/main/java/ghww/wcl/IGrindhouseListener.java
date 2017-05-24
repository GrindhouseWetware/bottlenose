package ghww.wcl;

import java.util.Queue;

public interface IGrindhouseListener {
    void dataChanged(Queue<String> msgQueue);
}
