package com.gigaspaces.gigapro.script_creator.service.profiles;

import com.gigaspaces.gigapro.script_creator.model.Profile;

import java.util.List;

public interface ProfilesService {

    String WIN_PATH_REPLACEMENT_PATTERN = "(jar:|file:/|!/)";

    String UNIX_PATH_REPLACEMENT_PATTERN = "(jar:|file:|!/)";

    List<Profile> getProfiles();
}
