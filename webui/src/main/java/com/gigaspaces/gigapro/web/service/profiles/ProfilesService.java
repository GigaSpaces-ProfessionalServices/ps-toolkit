package com.gigaspaces.gigapro.web.service.profiles;

import com.gigaspaces.gigapro.web.model.Profile;

import java.util.List;

public interface ProfilesService {

    String PATH_REPLACEMENT_PATTERN = "(jar:|file:/|!/)";

    List<Profile> getProfiles();

    List<String> getProfilesNames();

    Profile getProfile(String profileName);
}