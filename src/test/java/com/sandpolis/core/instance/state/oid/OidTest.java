//============================================================================//
//                                                                            //
//                         Copyright © 2015 Sandpolis                         //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPL    //
//  as published by the Mozilla Foundation.                                   //
//                                                                            //
//============================================================================//
package com.sandpolis.core.instance.state.oid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OidTest {

	@Test
	void testFirst() {
		assertEquals("test", Oid.of("/test/123").first());
	}

	@Test
	void testIsConcrete() {
		assertTrue(Oid.of("/test/123"));
		assertFalse(Oid.of("/test/*/123"));
	}

	@Test
	void testLast() {
		assertEquals("123", Oid.of("/test/123").last());
		assertEquals("123", Oid.of("/test/*/123").last());
	}

	@Test
	void testResolve() {
		assertEquals("com.sandpolis.core.instance:/test/a/b/*", Oid.of("/test/*/*/*").resolve("a", "b").toString());
	}

	@Test
	void testResolveLast() {
		assertEquals("com.sandpolis.core.instance:/test/*/a/b", Oid.of("/test/*/*/*").resolveLast("a", "b").toString());
	}

	@Test
	void testToString() {
		assertEquals("com.sandpolis.core.instance:/test/123", Oid.of("/test/123").toString());
		assertEquals("com.sandpolis.core.net:/test/123", Oid.of("com.sandpolis.core.net:/test/123").toString());
	}

}
