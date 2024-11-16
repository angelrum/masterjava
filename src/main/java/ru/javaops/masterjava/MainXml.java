package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.Group;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainXml {

    private static final JaxbParser JAXB_PARSER = new JaxbParser(ObjectFactory.class);

    static {
        JAXB_PARSER.setSchema(Schemas.ofClasspath("payload.xsd"));
    }

    public void JaxbParser(String project) throws Exception {
        Payload payload = JAXB_PARSER.unmarshal(
                Resources.getResource("payload.xml").openStream());
        payload.getGroups().getGroup().stream()
                .filter(group -> project.equals(group.getName()))
                .flatMap(group -> group.getUsers().getUser().stream())
                .map(Group.Users.User::getEmail)
                .sorted()
                .forEach(System.out::println);
    }

    public void StaxParser(String project) throws Exception {
        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())) {
            Map<String, String> users = new HashMap<>();
            XMLStreamReader reader = processor.getReader();
            boolean result = false;
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("Users".equals(reader.getLocalName())) {
                        getUsers(reader, users);
                    } else if ("Groups".equals(reader.getLocalName())) {
                        result = findProject(reader, users, project);
                        break;
                    }
                }
            }
            if (result) {
                users.forEach((key, value) -> System.out.printf("%s/%s\n", key, value));
            }
        }
    }

    private boolean findProject(XMLStreamReader reader, Map<String, String> users, String project) throws XMLStreamException {
        boolean process = false;
        do {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT
                && "Group".equals(reader.getLocalName())) {
                process = true;
            } else if (process
                       && event == XMLEvent.START_ELEMENT
                       && "name".equals(reader.getLocalName())) {
                if (project.equals(reader.getElementText())) {
                    processUsers(reader, users);
                    return true;
                } else {
                    process = false;
                }
            }
            if (event == XMLEvent.END_ELEMENT
                && "Groups".equals(reader.getLocalName()))
                break;
        } while (true);
        return false;
    }

    private void processUsers(XMLStreamReader reader, Map<String, String> users) throws XMLStreamException {
        Set<String> emails = new HashSet<>();
        do {
            int event = reader.next();
            if (event == XMLEvent.END_ELEMENT && "Users".equals(reader.getLocalName())) {
                break;
            } else if (event == XMLEvent.START_ELEMENT
                && "User".equals(reader.getLocalName())) {
                emails.add(reader.getAttributeValue(null, "email"));
            }
        } while (true);
        if (!emails.isEmpty()) {
            users.entrySet().removeIf(pair -> !emails.contains(pair.getKey()));
        }
    }

    private void getUsers(XMLStreamReader reader, Map<String, String> users) throws XMLStreamException {
        boolean process = false;
        String email = "";
        do {
            int event = reader.next();
            if (event == XMLEvent.START_ELEMENT && "User".equals(reader.getLocalName()))
                process = true;
            else if (event == XMLEvent.END_ELEMENT && "User".equals(reader.getLocalName())) {
                process = false;
            }
            if (process && event == XMLEvent.START_ELEMENT) {
                if (reader.getLocalName().equals("email")) {
                    email = reader.getElementText();
                } else if (reader.getLocalName().equals("fullName")) {
                    users.put(email, reader.getElementText());
                }
            }
            if (event == XMLEvent.END_ELEMENT
                && "Users".equals(reader.getLocalName()))
                break;
        } while (true);
    }
}
