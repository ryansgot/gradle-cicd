# gradle-cicd
One common theme that I see in industry is that people tend to heavily invest in a continuous integration/deployment pipeline or other such system to the extent that they mix the concern of building the project and automating the build. For example, if you cannot perform the stages of your build except that the stage be run on a CI/CD-dedicated mahcine, you now have a point of failure that can ultimately slow down development and magnify the delay caused by CI/CD maintenance.

Additionally, if you marry the concepts of your build and your CI/CD tool, then you make it difficult to migrate to a new tool. It is the developer's opinion that CI/CD should be about automation only, and should not be integral to the building, testing, packaging or deployment of your project. Instead, building, testing, packaging, and deployment _should_ be integrated into your build tool.

Thankfully, with gradle, there are plugins that do just about everything.