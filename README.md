# ITA Controlled English (CE)

The CE Store contains everything you need to get started with the ITA Controlled English environment.

We are an IBM initiated open source organisation focused on providing an experimental research environment for the ITA Controlled English language.

## Installation and Setup

Clone the code

```
git clone https://github.com/ce-store/ce-store
```

### Eclipse

Install Eclipse

  1. Download and install [Eclipse IDE for Java EE Developers](http://www.eclipse.org/downloads/packages/eclipse-ide-java-ee-developers/mars1).
  2. Open Eclipse and set up your workspace.

Import the project into Eclipse

  1. In Eclipse select `File` then `Import`
  2. Under the `Import` window select `Git` then `Projects from Git`
  3. Select `Existing local repository`
  4. If `ce-store` is not listed click `Add...` and locate folder
  5. Click `Finish`
  6. Under the `Select a wizard` window select `Java Project`
  7. Name your project
  8. Click `Finish`

The project should now appear in your Package Explorer.

Add the project to your favourite server and run. Liberty and Tomcat examples are described below.

### Liberty

Install Liberty

  1. Go to the [Liberty Get Started page](https://developer.ibm.com/wasdev/downloads/liberty-profile-using-eclipse/) and follow instructions to install

Set up Liberty server

  1. Set up a new server by clicking `File`, `New`, `Other...`, `Server`, `Server`
  2. Click `Next`
  3. Under `IBM` select `WebSphere Application Server Liberty`
  4. Click `Next`
  5. Select `Install from an archive or a repository` (If you already have the Liberty Runtime installed select the runtime and skip to step 12)
  6. Click `Next`
  7. Select `Download and install a new runtime environment from ibm.com`
  8. Select `WAS Liberty V8.5.* Runtime`
  9. Enter a destination path for the installation
  10. Click `Next`, then `Next` again
  11. Accept the T&Cs
  12. Name your server and click `Next`
  13. Add `ce-store` to configure on the server
  14. Click `Finish`

Run the server

  1. In the Server view right click the Liberty server and click `Start`
  2. Access the CE Store at [http://localhost:9080/ce-store](http://localhost:9080/ce-store)

### Tomcat

Install Tomcat

  1. Go to the [Tomcat website](http://tomcat.apache.org/) and download Tomcat 7 (minimum required version).

Add Tomcat to Eclispe

  1. In Eclipse open `Eclipse`, `Preferences`, `Server`, `Runtime Environments`
  2. Click `Add...`
  3. Under the New Server Runtime dialog select your runtime under Apache (minimum Tomcat 7)
  4. Click `Next`
  5. Fill in the Tomcat installation directory
  6. Ensure the selected JRE is a full JDK and is a version that will satisfy Apache Tomcat. If necessary, you can click on Installed JREs... to add JDKs to Eclipse
  7. Click `Finish`

Set up Tomcat server

  1. Set up a new server by clicking `File`, `New`, `Other...`, `Server`, `Server`
  2. Click `Next`
  3. Under `Apache` select `Tomcat`
  4. Name your server and click `Next`
  5. Add `ce-store` to configure on the server
  6. Click `Finish`

Run the server

  1. In the Server view right click the Tomcat server and click `Start`
  2. Access the CE Store at [http://localhost:8080/ce-store](http://localhost:8080/ce-store)

## Usage

### Engineering Panel

The Engineering Panel provides a simple web interface to the CE Store, enabling the user view the contents of the CE Store, to load new CE sentences, run queries agains the store, and more.

To open the Engineering Panel go to `<SERVER>/ce-store/ui`. It should look like this:

![Engineering Panel](http://ce-store.github.io/i/ui.png)

TODO: How tos & examples

## Contributing

Read about contributing [here](https://github.com/ce-store/ce-store/blob/master/CONTRIBUTE.md).

## License

Licensed under the [Apache License, Version 2.0](https://github.com/ce-store/ce-store/blob/master/LICENSE.md)
