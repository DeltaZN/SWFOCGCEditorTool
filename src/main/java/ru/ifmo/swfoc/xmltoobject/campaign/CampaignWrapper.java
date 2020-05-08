package ru.ifmo.swfoc.xmltoobject.campaign;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@Data
@XmlRootElement(name = "Campaigns")
@XmlAccessorType(XmlAccessType.FIELD)
public class CampaignWrapper implements Serializable {
    private String fileName;
    @XmlElement(name = "Campaign")
    private List<Campaign> campaigns;
}