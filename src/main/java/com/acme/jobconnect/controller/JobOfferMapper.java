package com.acme.jobconnect.controller;

import com.acme.jobconnect.entity.Application;
import com.acme.jobconnect.entity.JobDescription;
import com.acme.jobconnect.entity.JobOffer;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import static org.mapstruct.NullValueMappingStrategy.RETURN_DEFAULT;

/// Mapper zwischen Entity-Klassen.
/// Siehe `build\generated\sources\annotationProcessor\java\main\...\KundeMapperImpl.java`.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@Mapper(nullValueIterableMappingStrategy = RETURN_DEFAULT, componentModel = "spring")
@AnnotateWith(ExcludeFromJacocoGeneratedReport.class)
interface JobOfferMapper {
    /// Convert DTO-Object by [JobOfferDTO] into [JobOffer] Object.
    ///
    /// @param dto DTO-Objet for `JobOfferDTO` without ID
    /// @return Converted `JobOffer-Object` with id == null
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    JobOffer toJobOffer(JobOfferDTO dto);

    /// Convert DTO-Object by [JobDescriptionDTO] into [JobDescription] Object.
    ///
    /// @param dto DTO-Objet for `JobDescriptionDTO`
    /// @return Converted `JobDescription-Object`
    @Mapping(target = "id", ignore = true)
    JobDescription toJobDescription(JobDescriptionDTO dto);

    /// Convert DTO-Object by [ApplicationDTO] into [Application] Object.
    ///
    /// @param dto DTO-Objet for `ApplicationDTO`
    /// @return Converted `Application-Object`
    @Mapping(target = "id", ignore = true)
    Application toApplication(ApplicationDTO dto);
}
