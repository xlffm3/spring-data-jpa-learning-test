package com.learning.jpa.domain.family;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import javax.persistence.NoResultException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class ParentTest {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("Cascade Persist 옵션일 때")
    @Nested
    class Describe_CascadePersist {

        @DisplayName("Child의 Parent를 Set하지 않으면")
        @Nested
        class Context_NotMappingParent {

            @DisplayName("Parent 영속화시 Child는 Parent 맵핑되지 않은채 영속화된다.")
            @Test
            void addChild_Mapping_Null() {
                // given
                Parent parent = new Parent("parent");
                Child child = new Child("child");
                parent.add(child);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                Parent findParent = parentRepository.findById(parent.getId()).get();
                Child findChild = testEntityManager.getEntityManager()
                    .createQuery("select c from Child c where c.id = :id", Child.class)
                    .setParameter("id", child.getId())
                    .getSingleResult();
                List<Child> childs = findParent.getChildren();

                // then
                assertThat(childs).isEmpty();
                assertThat(findChild)
                    .usingRecursiveComparison()
                    .isEqualTo(new Child(findChild.getId(), "child", null));
            }
        }

        @DisplayName("Child의 Parent를 Set하면")
        @Nested
        class Context_MappingParent {

            @DisplayName("Parent 영속화시 Child는 Parent 맵핑된채 영속화된다.")
            @Test
            void addChild_Mapping_Parent() {
                // given
                Parent parent = new Parent("parent");
                Child child = new Child("child");
                child.toParent(parent);
                parent.add(child);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                Parent findParent = parentRepository.findById(parent.getId()).get();
                Child findChild = testEntityManager.getEntityManager()
                    .createQuery("select c from Child c where c.id = :id", Child.class)
                    .setParameter("id", child.getId())
                    .getSingleResult();
                List<Child> childs = findParent.getChildren();

                // then
                assertThat(childs).isNotEmpty();
                assertThat(findChild.getId()).isNotNull();
            }
        }
    }

    @DisplayName("Cascade Remove 옵션일 때")
    @Nested
    class Describe_CascadeRemove {

        @DisplayName("Parent가 삭제되면")
        @Nested
        class Context_ParentRemoved {

            @DisplayName("Child 또한 함께 삭제된다.")
            @Test
            void removeParent_ChildRemoved_True() {
                // given
                Parent parent = new Parent("parent");
                Child child = new Child("child");
                child.toParent(parent);
                parent.add(child);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                parentRepository.deleteById(parent.getId());

                testEntityManager.flush();
                testEntityManager.clear();

                // then
                assertThatCode(() -> {
                    testEntityManager.getEntityManager()
                        .createQuery("select c from Child c where c.id = :id", Child.class)
                        .setParameter("id", child.getId())
                        .getSingleResult();
                }).isInstanceOf(NoResultException.class);
            }
        }

        @DisplayName("Parent의 컬렉션에서 Child를 삭제하면")
        @Nested
        class Context_ChildRemoved {

            @DisplayName("Child는 삭제되지 않는다.")
            @Test
            void removeChild_NotDeleted_True() {
                // given
                Parent parent = new Parent("parent");
                Child child = new Child("child");
                child.toParent(parent);
                parent.add(child);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                Parent findParent = parentRepository.findById(parent.getId()).get();
                Child findChild = findParent.getChildren().get(0);
                findParent.remove(findChild);

                testEntityManager.flush();
                testEntityManager.clear();

                // then
                Parent findParent2 = parentRepository.findById(parent.getId()).get();
                Child findChild2 = testEntityManager.getEntityManager()
                    .createQuery("select c from Child c where c.id = :id", Child.class)
                    .setParameter("id", findChild.getId())
                    .getSingleResult();

                assertThat(findParent2.getChildren()).isNotEmpty();
                assertThat(findChild2.getId()).isNotNull();
            }
        }
    }

    @DisplayName("orphanRemoval이 true면")
    @Nested
    class Describe_orphanRemovalActivated {

        @DisplayName("부모 엔티티가 삭제되었을 때")
        @Nested
        class Context_ParentEntityRemoved {

            @DisplayName("자식 엔티티가 삭제된다.")
            @Test
            void removeParent_ChildEntityDeleted_True() {
                // given
                Parent parent = new Parent("parent");
                Orphan orphan = new Orphan("orphan", parent);
                parent.add(orphan);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                parentRepository.deleteById(parent.getId());

                testEntityManager.flush();
                testEntityManager.clear();

                // then
                assertThatCode(() -> {
                    testEntityManager.getEntityManager()
                        .createQuery("select o from Orphan o where o.id = :id", Orphan.class)
                        .setParameter("id", orphan.getId())
                        .getSingleResult();
                }).isInstanceOf(NoResultException.class);

            }
        }

        @DisplayName("부모 엔티의 컬렉션에서 자식 엔티티를 제거할 때")
        @Nested
        class Context_ChildRemoved {

            @DisplayName("자식 엔티티가 삭제된다.")
            @Test
            void removeChild_ChildEntityDeleted_True() {
                // given
                Parent parent = new Parent("parent");
                Orphan orphan = new Orphan("orphan", parent);
                parent.add(orphan);
                parentRepository.save(parent);

                testEntityManager.flush();
                testEntityManager.clear();

                // when
                Parent findParent = parentRepository.findById(parent.getId()).get();
                Orphan findOrphan = findParent.getOrphans().get(0);
                assertThat(findOrphan.getId()).isNotNull();
                findParent.remove(findOrphan);

                testEntityManager.flush();
                testEntityManager.clear();

                // then

                assertThatCode(() -> {
                    testEntityManager.getEntityManager()
                        .createQuery("select o from Orphan o where o.id = :id", Orphan.class)
                        .setParameter("id", orphan.getId())
                        .getSingleResult();
                }).isInstanceOf(NoResultException.class);
            }
        }
    }
}
