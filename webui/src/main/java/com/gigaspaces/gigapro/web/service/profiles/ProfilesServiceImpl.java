package com.gigaspaces.gigapro.web.service.profiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigaspaces.gigapro.web.model.Profile;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

@Service
public class ProfilesServiceImpl implements ProfilesService {

    private static final ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper();

    @Override
    public List<Profile> getProfiles() {
        try {
            List<Profile> profiles = new ArrayList<>();
            Path profilesPath = getProfilesLocationPath();
            try (DirectoryStream<Path> directoryStream = newDirectoryStream(profilesPath)) {
                directoryStream.forEach(p -> profiles.add(createProfile(p)));
            }
            return profiles;
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error occurred obtaining profiles directory path", e);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred reading profiles", e);
        }
    }

    @Override
    public List<String> getProfilesNames() {
        return getProfiles().stream().map(Profile::getName).collect(toList());
    }

    @Override
    public Profile getProfile(String profileName) {
        return getProfiles().stream().filter(p -> p.getName().equals(profileName)).findFirst().get();
    }

    private Path getProfilesLocationPath() throws URISyntaxException {
        String applicationPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        String applicationLocationPath = replacePattern(applicationPath, PATH_REPLACEMENT_PATTERN, EMPTY);
        Path profilesPath = Paths.get(applicationLocationPath);
        while ( !exists(profilesPath.resolve("config/profiles"))) {
            profilesPath = profilesPath.getParent();
        }
        return profilesPath.resolve("config/profiles");
    }

    private Profile createProfile(Path path) {
        Profile profile = new Profile();
        profile.setName(removeEnd(path.getFileName().toString(), ".json"));
        profile.setOptions(parseOptions(path));
        return profile;
    }

    private XapConfigOptions parseOptions(Path path) {
        try {
            return JSON_OBJECT_MAPPER.readValue(path.toFile(), XapConfigOptions.class);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred parsing profile " + path.toString(), e);
        }
    }
}
