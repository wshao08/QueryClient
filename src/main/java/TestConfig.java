
public class TestConfig {
    public String serverUrl;
    public int loop;
    public int startTime;
    public int endTime;

    public TestConfig() {
    }

    public String getUrl() {
        return serverUrl;
    }

    public void setUrl(String url) {
        this.serverUrl = url;
    }

    public int getLoop() {
        return loop;
    }

    public void setLoop(int loop) {
        this.loop = loop;
    }

    public int getStart() {
        return startTime;
    }

    public void setStart(int start) {
        this.startTime = startTime;
    }

    public int getEnd() {
        return endTime;
    }

    public void setEnd(int end) {
        this.endTime = end;
    }
}
