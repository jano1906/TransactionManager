package cp1.solution;

public class AbortedThreadIndicator {
    private boolean isAborted;
    public AbortedThreadIndicator(){
        this.isAborted = false;
    }
    public void abort(){
        isAborted = true;
    }
    public boolean value(){
        return isAborted;
    }
}
