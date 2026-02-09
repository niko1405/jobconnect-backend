package com.acme.jobconnect.repository;

import com.acme.jobconnect.entity.JobOffer;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import static com.acme.jobconnect.entity.JobOffer.DESCRIPTION_APPLICATIONS_GRAPH;
import static com.acme.jobconnect.entity.JobOffer.JOBDESCRIPTION_GRAPH;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID>, JpaSpecificationExecutor<JobOffer> {
    @EntityGraph(JOBDESCRIPTION_GRAPH)
    @Override
    Page<JobOffer> findAll(Pageable pageable);

    @EntityGraph(JOBDESCRIPTION_GRAPH)
    @Override
    Page<JobOffer> findAll(@Nullable Specification<JobOffer> spec, Pageable pageable);

    @Query("""
        SELECT k
        FROM   #{#entityName} k
        WHERE  k.id = :id
        """)
    @EntityGraph(JOBDESCRIPTION_GRAPH)
    @Nullable
    JobOffer findByIdFetchJobDescription(UUID id);

    @Query("""
        SELECT k
        FROM   #{#entityName} k
        WHERE  k.id = :id
        """)
    @EntityGraph(DESCRIPTION_APPLICATIONS_GRAPH)
    @Nullable
    JobOffer findByIdFetchJobDescriptionAndApplications(UUID id);

    @Query("""
        SELECT   j
        FROM     #{#entityName} j
        WHERE    lower(j.company) LIKE concat(lower(:company), '%')
        ORDER BY j.company
        """)
    @EntityGraph(JOBDESCRIPTION_GRAPH)
    Page<JobOffer> findByCompay(String company, Pageable pageable);

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean existsByCompany(String company);

    @Query("""
        SELECT  CASE WHEN COUNT(j) > 0 THEN true ELSE false END
        FROM    JobOffer j
        WHERE   j.company = :company AND j.description.title = :title
        """)
    boolean existsByCompanyAndTitle(String company, String title);

}
