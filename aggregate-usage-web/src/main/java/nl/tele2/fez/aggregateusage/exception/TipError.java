package nl.tele2.fez.aggregateusage.exception;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "error")
public class TipError {

    private String error;

    public String getError() {
        return error;
    }

    @XmlValue
    public void setError(String error) {
        this.error = error;
    }
}
