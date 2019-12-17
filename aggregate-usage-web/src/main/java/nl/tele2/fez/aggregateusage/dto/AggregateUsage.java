package nl.tele2.fez.aggregateusage.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import nl.tele2.fez.aggregateusage.dto.bundles.AggregatedDataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.Bundle;
import nl.tele2.fez.aggregateusage.dto.bundles.DataBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.ExtraCostsBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.SmsBundle;
import nl.tele2.fez.aggregateusage.dto.bundles.VoiceBundle;
import nl.tele2.fez.aggregateusage.dto.usage.Usage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AggregateUsage {
    private String msisdn;

    private BillingPeriod billingPeriod;

    private UnbilledUsage unbilledUsage;

    @ApiModelProperty(value = "Timestamp when the usage was last retrieved", example = "2019-01-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @ApiModelProperty(value = "Contains the national (and eu) bundles aggregated")
    private AggregatedDataBundle data;
    @ApiModelProperty(value = "Contains the eu limit for an unlimited data customer")
    private DataBundle dataLimitEu;
    @ApiModelProperty(value = "Contains the combined voice and sms bundle")
    private VoiceBundle voiceSms;
    @ApiModelProperty(value = "Contains the costs that are made outside of the bundle")
    private ExtraCostsBundle extraCosts;
    @ApiModelProperty(value = "Contains the SMS bundle of customer (!This does not contain voice!")
    private SmsBundle sms;
    @ApiModelProperty(value = "List of the roaming bundles aggregated per zone")
    private List<AggregatedDataBundle> roamingBundles = new ArrayList<>();

    public boolean isUnlimitedVoice() {
        return voiceSms != null && voiceSms.getUsage().isUnlimited();
    }

    public boolean isUnlimitedData() {
        return data != null && data.getBundles().stream()
                .filter(bundle -> !data.getUnlimited24Hours())
                .map(Bundle::getUsage)
                .anyMatch(Usage::isUnlimited);
    }
}
