subprojects {
    base { archivesName.set("${rootProject.base.archivesName.get()}-${project.name}") }
}