package com.gigaspaces.gigapro.script_creator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private String name;
    private XapConfigOptions options;
}
