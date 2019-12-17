package nl.tele2.fez.aggregateusage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MsisdnsResponse implements Serializable {

    @JsonProperty("MSISDNList")
    private List<String> msisdns = new ArrayList<>();
    @JsonProperty("inactiveMSISDNList")
    private List<String> inactiveMsisdns = new ArrayList<>();
    @JsonProperty("suspendedMSISDNList")
    private List<String> suspendedMsisdns = new ArrayList<>();
}
