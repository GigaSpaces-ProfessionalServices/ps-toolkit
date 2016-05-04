package com.gigaspaces.gigapro.script_creator.service.profiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigaspaces.gigapro.script_creator.model.Profile;
import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Paths.get;
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

    private Path getProfilesLocationPath() throws URISyntaxException {
        String applicationPath = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        String applicationLocationPath;
        if (SystemUtils.IS_OS_UNIX) {
            applicationLocationPath = replacePattern(applicationPath, UNIX_PATH_REPLACEMENT_PATTERN, EMPTY);
        } else {
            applicationLocationPath = replacePattern(applicationPath, WIN_PATH_REPLACEMENT_PATTERN, EMPTY);
        }
        Path profilesPath = get(applicationLocationPath);

        while ( !exists(profilesPath.resolve("config/profiles"))) {
            profilesPath = profilesPath.getParent();
            if (profilesPath == null) {
                throw new RuntimeException("Haven't found /config/profiles directory");
            }
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
