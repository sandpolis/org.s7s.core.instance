//============================================================================//
//                                                                            //
//            Copyright © 2015 - 2022 Sandpolis Software Foundation           //
//                                                                            //
//  This source file is subject to the terms of the Mozilla Public License    //
//  version 2. You may not use this file except in compliance with the MPLv2. //
//                                                                            //
//============================================================================//
package org.s7s.core.instance.state.oid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OidTest {

	@Test
	void testFirst() {
		assertEquals("test", Oid.of("/test/123").first());
	}

	@Test
	void testIsConcrete() {
		assertTrue(Oid.of("/test/123").isConcrete());
		assertFalse(Oid.of("/test/*/123").isConcrete());
	}

	@Test
	void testLast() {
		assertEquals("123", Oid.of("/test/123").last());
		assertEquals("123", Oid.of("/test/*/123").last());
	}

	@Test
	void testResolve() {
		assertEquals("org.s7s.core.instance:/test/a/b/*", Oid.of("/test/*/*/*").resolve("a", "b").toString());
	}

	@Test
	void testResolveLast() {
		assertEquals("org.s7s.core.instance:/test/*/a/b", Oid.of("/test/*/*/*").resolveLast("a", "b").toString());
	}

	@Test
	void testToString() {
		assertEquals("org.s7s.core.instance:/test/123", Oid.of("/test/123").toString());
		assertEquals("org.s7s.core.instance:/test/123", Oid.of("org.s7s.core.instance:/test/123").toString());
	}

}
