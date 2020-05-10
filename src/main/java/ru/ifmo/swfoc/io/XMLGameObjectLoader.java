package ru.ifmo.swfoc.io;

import lombok.Data;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import ru.ifmo.swfoc.xmltoobject.Unit;
import ru.ifmo.swfoc.xmltoobject.campaign.CampaignWrapper;
import ru.ifmo.swfoc.xmltoobject.faction.FactionWrapper;
import ru.ifmo.swfoc.xmltoobject.planet.Planet;
import ru.ifmo.swfoc.xmltoobject.planet.PlanetWrapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
public class XMLGameObjectLoader {
    private Config config;
    private File processingFile;
    private List<Unit> squadrons = new ArrayList<>();
    private List<Unit> spaceUnits = new ArrayList<>();
    private List<Unit> specialStructures = new ArrayList<>();
    private List<Unit> groundCompanies = new ArrayList<>();
    private List<Unit> starBases = new ArrayList<>();
    private List<Planet> planets = new ArrayList<>();
    private List<Unit> heroCompanies = new ArrayList<>();

    public XMLGameObjectLoader(File file, Config config) {
        processingFile = file;
        this.config = config;
    }

    public void readAllGameObjects() {
        SAXBuilder builder = new SAXBuilder();

        try {
            Document document = builder.build(processingFile);
            Element rootNode = document.getRootElement();
            List<Element> list = rootNode.getChildren();

            for (Element node : list) {
                String gameObjectFileName = node.getValue();
                File gameObjectFile = config.findFileIgnoreCase(gameObjectFileName);

                Document gameObjectDoc = builder.build(gameObjectFile);
                Element rootNodeGameObject = gameObjectDoc.getRootElement();
                List<Element> listGameObject = rootNodeGameObject.getChildren();

                nextFile:
                for (Element gameObject : listGameObject) {
                    if (gameObject.getAttribute("Name").getValue().contains("Death_Clone"))
                        continue;
                    String xmlName = gameObject.getAttributeValue("Name");
                    String factions = gameObject.getChildText("Affiliation");
                    String textId = gameObject.getChildText("Text_ID");
                    boolean hasSpaceEvaluator = gameObject.getChild("Has_Space_Evaluator") != null;
                    Element variantOfExistingType = gameObject.getChild("Variant_Of_Existing_Type");

                    switch (gameObject.getName()) {
                        case "Squadron":
                            if (variantOfExistingType!= null) {
                                addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, squadrons);
                            } else squadrons.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                            break;
                        case "SpaceUnit":
                            addSpaceUnit(gameObject, xmlName, factions, textId, hasSpaceEvaluator, variantOfExistingType);
                            break;
                        case "SpecialStructure":
                            if (variantOfExistingType!= null) {
                                addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, specialStructures);
                            } else specialStructures.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                            break;
                        case "GroundCompany":
                            if (variantOfExistingType!= null) {
                                addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, groundCompanies);
                            } else groundCompanies.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                            break;
                        case "StarBase":
                            if (variantOfExistingType!= null) {
                                addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, starBases);
                            } else starBases.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                            break;
                        case "HeroCompany":
                            if (variantOfExistingType!= null) {
                                addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, heroCompanies);
                            } else heroCompanies.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                            break;
                        case "Planet":
                            addPlanet(gameObjectFile);
                            break nextFile;
                    }
                }
            }

        } catch (IOException | JDOMException io) {
            System.out.println(io.getMessage());
        }
    }

    private void addPlanet(File gameObjectFile) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(PlanetWrapper.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            PlanetWrapper planetWrapper = (PlanetWrapper) jaxbUnmarshaller.unmarshal(gameObjectFile);
            planets.addAll(planetWrapper.getPlanets());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void addSpaceUnit(Element gameObject, String xmlName, String factions, String textId, boolean hasSpaceEvaluator, Element variantOfExistingType) {
        if (variantOfExistingType!= null) {
            addUnitOfExistingType(gameObject, xmlName, factions, textId, hasSpaceEvaluator, spaceUnits);
        } else if (gameObject.getChild("Has_Space_Evaluator") != null)
            spaceUnits.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
    }

    private void addUnitOfExistingType(Element gameObject, String xmlName, String factions, String textId, boolean hasSpaceEvaluator, List<Unit> spaceUnits) {
        for (Unit unit : spaceUnits) {
            if (unit.getXmlName().equalsIgnoreCase(gameObject.getChildText("Variant_Of_Existing_Type").trim())) {
                if (factions == null)
                    factions = unit.getFaction();
                if (textId == null)
                    textId = unit.getTextId();
                spaceUnits.add(new Unit(xmlName, textId, factions, hasSpaceEvaluator));
                break;
            }
        }
    }
}
