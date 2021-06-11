package Entities;

import java.io.Serializable;
import java.util.Objects;

public class ControlMessage implements Serializable {

    


    @Override
    public int hashCode() {
        return Objects.hash(controlCommand, param, path);
    }

    private String controlCommand;
    private String param;
    private String path;

    public ControlMessage(String controlCommand){
        this.controlCommand = controlCommand;
    }

    public ControlMessage(String controlCommand, String param){
        this.controlCommand = controlCommand;
        this.param = param;
    }

    public ControlMessage(String controlCommand, String param, String path) {
        this.controlCommand = controlCommand;
        this.param = param;
        this.path = path;
    }

    public String getControlCommand() {
        return controlCommand;
    }

    public String getParam() {
        return param;
    }

    public String getPath() {
        return path;
    }
}
