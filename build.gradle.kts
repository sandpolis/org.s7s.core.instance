//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//

plugins {
	id("java-library")
	id("sandpolis-java")
	id("sandpolis-module")
	id("sandpolis-protobuf")
	id("sandpolis-publish")
	id("sandpolis-codegen")
}

dependencies {
	testImplementation("net.jodah:concurrentunit:0.4.6")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")

	// https://github.com/qos-ch/logback
	implementation("ch.qos.logback:logback-core:1.3.0-alpha9") {
		exclude(group = "edu.washington.cs.types.checker", module = "checker-framework")
	}
	implementation("ch.qos.logback:logback-classic:1.3.0-alpha5") {
		exclude(group = "edu.washington.cs.types.checker", module = "checker-framework")
		exclude(group = "com.sun.mail", module = "javax.mail")
	}

	if (project.getParent() == null) {
		api("com.sandpolis:core.foundation:+")
	} else {
		api(project(":module:com.sandpolis.core.foundation"))
	}
}
