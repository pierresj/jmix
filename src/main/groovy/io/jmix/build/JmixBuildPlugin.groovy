package io.jmix.build

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.JavadocMemberLevel

class JmixBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        JmixBuildExtension extension = project.extensions.create('jmixBuild', JmixBuildExtension)

        setupRepositories(project)
        setupCompilation(project, extension)
        setupTestExecution(project)
        setupJavadocsBuilding(project)
        setupPublishing(project)
        setupSpotbugs(project)
        setupAggregateJavadocsBuilding(project)
    }

    private void setupRepositories(Project project) {
        project.with {
            String jmixRepoUrl = rootProject.findProperty('jmixRepoUrl')
            if (jmixRepoUrl) {
                repositories {
                    maven {
                        url jmixRepoUrl
                        String jmixRepoUser = rootProject.findProperty('jmixRepoUser')
                        String jmixRepoPassword = rootProject.findProperty('jmixRepoPassword')
                        if (jmixRepoUser && jmixRepoPassword) {
                            credentials {
                                username jmixRepoUser
                                password jmixRepoPassword
                            }
                        }
                    }
                    mavenCentral()
                }
            } else {
                repositories {
                    mavenLocal()
                    maven {
                        url('https://nexus.jmix.io/repository/public')
                    }
                    mavenCentral()
                }
            }
        }
    }

    private void setupCompilation(Project project, JmixBuildExtension extension) {
        project.with {
            apply plugin: 'java'
            apply plugin: 'java-library'

            afterEvaluate {
                java {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
            }
        }

        project.tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
        }
    }

    private void setupPublishing(Project project) {
        if (!project.name.startsWith('sample')) {
            project.with {
                apply plugin: 'maven-publish'

                afterEvaluate {
                    java {
                        withSourcesJar()
                    }
                    artifacts {
                        archives sourcesJar
                    }
                    publishing {
                        String jmixUploadUrl = rootProject.findProperty('jmixUploadUrl')
                        if (jmixUploadUrl) {
                            repositories {
                                maven {
                                    url = jmixUploadUrl
                                    credentials {
                                        username rootProject['jmixUploadUser']
                                        password rootProject['jmixUploadPassword']
                                    }
                                    allowInsecureProtocol = true
                                }
                            }
                        }
                        publications {
                            javaMaven(MavenPublication) {
                                artifactId = archivesBaseName
                                from components.java
                            }
                        }
                    }
                }

                // remove after https://youtrack.jetbrains.com/issue/IDEA-227215 is fixed
                tasks.withType(GenerateModuleMetadata) {
                    enabled = false
                }
            }
        }
    }

    private void setupTestExecution(Project project) {
        project.tasks.withType(Test) {
            systemProperty('org.slf4j.simpleLogger.defaultLogLevel', 'debug')
            systemProperty('org.slf4j.simpleLogger.log.org.springframework', 'info')
            systemProperty('org.slf4j.simpleLogger.log.eclipselink.sql', 'debug')
        }
        project.with {
            afterEvaluate {
                test {
                    useJUnitPlatform {
                        boolean result = project.hasProperty("includeSlowTests") ? project.getProperty("includeSlowTests") : false
                        if (!result) {
                            excludeTags 'slowTests'
                        } else {
                            environment "slowTests", "true"
                        }
                    }
                }
            }
        }
    }

    private void setupJavadocsBuilding(Project project) {
        project.tasks.withType(Javadoc) {
            options.addStringOption("sourcepath", "")
            options.encoding = 'UTF-8'
            options.memberLevel = JavadocMemberLevel.PROTECTED
            destinationDir = project.file("$project.buildDir/docs/javadoc")

            title = "Jmix ${project.name} ${project.version.replace('-SNAPSHOT', '')} API"
        }
    }

    void setupAggregateJavadocsBuilding(Project project) {
        Project rootProject = project.rootProject
        if (rootProject) {
            rootProject.gradle.projectsEvaluated {
                Set<Task> existingTasks = rootProject.getTasksByName("aggregateJavadoc", false)
                if (existingTasks.isEmpty()) {
                    Set<Project> javaSubprojects = getJavaSubProjects(rootProject)
                    if (!javaSubprojects.isEmpty()) {
                        rootProject.task("aggregateJavadoc", type: Javadoc) {
                            description = 'Aggregates Javadoc API documentation of all subprojects.'
                            group = JavaBasePlugin.DOCUMENTATION_GROUP
                            options.encoding = 'UTF-8'
                            options.memberLevel = JavadocMemberLevel.PROTECTED
                            options.addStringOption("sourcepath", "")

                            dependsOn javaSubprojects.javadoc
                            source javaSubprojects.javadoc.source

                            destinationDir = rootProject.file("$rootProject.buildDir/docs/javadoc")
                            classpath = rootProject.files(javaSubprojects.javadoc.classpath)

                            title = "${rootProject.name.capitalize()} ${rootProject.version.replace('-SNAPSHOT', '')} API"

                            if (rootProject.hasProperty('javadocPublishCmd')) {
                                doLast {
                                    rootProject.exec {
                                        workingDir "$rootProject.buildDir/docs/javadoc"
                                        commandLine 'sh', '-c', rootProject.javadocPublishCmd
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setupSpotbugs(Project project) {
        project.with {
            if (rootProject.hasProperty('spotbugsEnabled')) {
                apply plugin: 'com.github.spotbugs'

                project.afterEvaluate {
                    //check that spotbugs-exclude-filter.xml is defined in the project. If not then copy the file from plugin resources
                    def excludeFilterFilePath = '../etc/spotbugs/spotbugs-exclude-filter.xml'
                    if (!file(excludeFilterFilePath).exists()) {
                        excludeFilterFilePath = "$project.buildDir/spotbugs/spotbugs-exclude-filter.xml"
                        def pluginExcludeFilterFileContent = getClass().getClassLoader().getResource('spotbugs/spotbugs-exclude-filter.xml').text
                        tasks.register('initSpotbugsExcludeFilter') {
                            doLast {
                                def excludeFilterFile = new File(excludeFilterFilePath)
                                excludeFilterFile.getParentFile().mkdirs()
                                excludeFilterFile.createNewFile()
                                excludeFilterFile.withWriter('utf-8') { writer ->
                                    writer.write pluginExcludeFilterFileContent
                                }
                            }
                        }
                        tasks.named('spotbugsMain').get().dependsOn('initSpotbugsExcludeFilter')
                    }

                    spotbugs {
                        toolVersion = '4.2.1'
                        ignoreFailures = false
                        omitVisitors = ['FindDoubleCheck']
                        excludeFilter = project.file(excludeFilterFilePath)
                        effort = "max"
                        reportLevel = "medium"
                    }

                    spotbugsMain {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            xml.enabled = false
                            html {
                                enabled = true
                                stylesheet = 'fancy-hist.xsl'
                                destination file("${project.buildDir}/reports/spotbugs/${project.name}.html")
                            }
                        }
                    }

                    spotbugsTest {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            xml.enabled = false
                            html {
                                enabled = true
                                stylesheet = 'fancy-hist.xsl'
                                destination file("${project.buildDir}/reports/spotbugs/test-${project.name}.html")
                            }
                        }
                    }
                }
            }
        }
    }

    private Set<Project> getJavaSubProjects(Project rootProject) {
        rootProject.subprojects.findAll { subproject -> subproject.plugins.hasPlugin(JavaPlugin) }
    }
}
