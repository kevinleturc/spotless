/*
 * Copyright 2020-2023 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.gradle.spotless;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TargetExcludeIfContentContainsTest extends GradleIntegrationHarness {
	@Test
	void targetExcludeIfContentContainsWithOneValue() throws IOException {
		setFile("build.gradle").toLines(
				"plugins { id 'com.diffplug.spotless' }",
				"spotless {",
				"  format 'toLower', {",
				"    target '**/*.md'",
				"    targetExcludeIfContentContains '// Generated by Mr. Roboto'",
				"    custom 'lowercase', { str -> str.toLowerCase() }",
				"  }",
				"}");
		String content = "// Generated by Mr. Roboto, do not edit.\n" +
				"A B C\n" +
				"D E F\n" +
				"G H I";
		setFile("test_generated.md").toContent(content);
		setFile("test_manual.md").toLines(
				"A B C",
				"D E F",
				"G H I");
		gradleRunner().withArguments("spotlessApply").build();
		// `test_generated` contains the excluding text so didn't change.
		assertFile("test_generated.md").hasContent(content);
		// `test_manual` does not so it changed.
		assertFile("test_manual.md").hasLines(
				"a b c",
				"d e f",
				"g h i");
	}

	@Test
	void targetExcludeIfContentContainsWithMultipleSteps() throws IOException {
		setFile("build.gradle").toLines(
				"plugins { id 'com.diffplug.spotless' }",
				"spotless {",
				"  format 'toLower', {",
				"    target '**/*.md'",
				"    targetExcludeIfContentContains '// Generated by Mr. Roboto'",
				"    custom 'lowercase', { str -> str.toLowerCase() }",
				"    licenseHeader('" + "// My CopyRights header" + "', '--')",
				"  }",
				"}");
		String generatedContent = "// Generated by Mr. Roboto, do not edit.\n" +
				"--\n" +
				"public final   class MyMessage {}\n";
		setFile("test_generated.md").toContent(generatedContent);
		String manualContent = "// Typo in License\n" +
				"--\n" +
				"public final class MyMessage {\n" +
				"}";
		setFile("test_manual.md").toContent(manualContent);
		gradleRunner().withArguments("spotlessApply").build();

		// `test_generated` contains the excluding text so didn't change, including the header.
		assertFile("test_generated.md").hasContent(generatedContent);
		// `test_manual` does not, it changed.
		assertFile("test_manual.md").hasContent(
				"// My CopyRights header\n" +
						"--\n" +
						"public final class mymessage {\n" +
						"}");
	}

	@Test
	void targetExcludeIfContentContainsWithMultipleValues() throws IOException {
		setFile("build.gradle").toLines(
				"plugins { id 'com.diffplug.spotless' }",
				"spotless {",
				"  format 'toLower', {",
				"    target '**/*.md'",
				"    targetExcludeIfContentContains '// Generated by Mr. Roboto|// Generated by Mrs. Call'",
				"    custom 'lowercase', { str -> str.toLowerCase() }",
				"  }",
				"}");
		String robotoContent = "A B C\n" +
				"// Generated by Mr. Roboto, do not edit.\n" +
				"D E F\n" +
				"G H I";
		setFile("test_generated_roboto.md").toContent(robotoContent);
		String callContent = "A B C\n" +
				"D E F\n" +
				"// Generated by Mrs. Call, do not edit.\n" +
				"G H I";
		setFile("test_generated_call.md").toContent(callContent);
		String collaborationContent = "A B C\n" +
				"// Generated by Mr. Roboto, do not edit.\n" +
				"D E F\n" +
				"// Generated by Mrs. Call, do not edit.\n" +
				"G H I";
		setFile("test_generated_collaboration.md").toContent(collaborationContent);
		String intruderContent = "A B C\n" +
				"// Generated by K2000, do not edit.\n" +
				"D E F\n" +
				"G H I";
		setFile("test_generated_intruder.md").toContent(intruderContent);
		setFile("test_manual.md").toLines(
				"A B C",
				"D E F",
				"G H I");
		gradleRunner().withArguments("spotlessApply").build();
		// Part of the excluding values so has not changed.
		assertFile("test_generated_roboto.md").hasContent(robotoContent);
		// Part of the excluding values so has not changed.
		assertFile("test_generated_call.md").hasContent(callContent);
		// Part of the excluding values so has not changed.
		assertFile("test_generated_collaboration.md").hasContent(collaborationContent);
		// Not part of the excluding values so has changed.
		assertFile("test_generated_intruder.md").hasContent(
				"a b c\n" +
						"// generated by k2000, do not edit.\n" +
						"d e f\n" +
						"g h i");
		// `test_manual` does not, it changed.
		assertFile("test_manual.md").hasLines(
				"a b c",
				"d e f",
				"g h i");
	}
}
