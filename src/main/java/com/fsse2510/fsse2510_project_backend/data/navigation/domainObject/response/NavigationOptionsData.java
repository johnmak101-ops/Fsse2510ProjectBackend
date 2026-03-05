package com.fsse2510.fsse2510_project_backend.data.navigation.domainObject.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationOptionsData {
    private List<String> collections;
    private List<String> categories;
    private List<String> tags;
    private List<String> productTypes;
}
