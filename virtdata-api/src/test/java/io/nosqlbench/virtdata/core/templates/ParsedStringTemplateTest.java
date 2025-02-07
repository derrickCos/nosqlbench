/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.engine.api.templating.BindType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ParsedStringTemplateTest {

    private final Map<String, String> bindings = Map.of(
            "bindname1", "bindspec1",
            "bindname2", "bindspec2");

    @Test
    public void testShouldMatchRawLiteral() {
        String rawNothing = "This has no anchors";
        ParsedStringTemplate pt = new ParsedStringTemplate(rawNothing, bindings);
        assertThat(pt.getSpans()).containsExactly("This has no anchors");
        assertThat(pt.getBindPoints()).isEmpty();
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldIgnoreExtraneousAnchors() {
        String oneExtraneous = "An {this is an extraneous form} invalid anchor.";
        ParsedStringTemplate pt = new ParsedStringTemplate(oneExtraneous, bindings);
        assertThat(pt.getSpans()).containsExactly("An {this is an extraneous form} invalid anchor.");
        assertThat(pt.getBindPoints()).isEmpty();
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldAllowArbitraryNonGreedyInExtendedBindPoint() {
        String oneExtendedBindPoint = "An {{this is an extended form}} {{and another}} invalid anchor.";
        ParsedStringTemplate pt = new ParsedStringTemplate(oneExtendedBindPoint, bindings);
        assertThat(pt.getSpans()).containsExactly("An ","this is an extended form"," ","and another"," invalid anchor.");
        assertThat(pt.getAnchors()).containsExactly("this is an extended form","and another");
    }

    @Test
    public void testShouldMatchLiteralVariableOnly() {
        String literalVariableOnly = "literal {bindname1}";
        ParsedStringTemplate pt = new ParsedStringTemplate(literalVariableOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("literal ", "bindname1", "");
        assertThat(pt.getAnchors()).containsOnly("bindname1");
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testShouldMatchVariableLiteralOnly() {
        String variableLiteralOnly = "{bindname2} literal";
        ParsedStringTemplate pt = new ParsedStringTemplate(variableLiteralOnly, bindings);
        assertThat(pt.getSpans()).containsExactly("", "bindname2", " literal");
        assertThat(pt.getAnchors()).containsOnly("bindname2");
        assertThat(pt.getMissing()).isEmpty();
    }

    @Test
    public void testPositionalExpansionShouldBeValid() {
        String multi = "A {bindname1} of {bindname2} sort.";
        ParsedStringTemplate pt = new ParsedStringTemplate(multi, bindings);
        assertThat(pt.getSpans()).containsExactly("A ", "bindname1", " of ", "bindname2", " sort.");
        assertThat(pt.getAnchors()).containsOnly("bindname1", "bindname2");
        assertThat(pt.getMissing()).isEmpty();
        assertThat(pt.getPositionalStatement(s -> "##")).isEqualTo("A ## of ## sort.");
        assertThat(pt.getPositionalStatement(s -> "[[" + s + "]]")).isEqualTo("A [[bindname1]] of [[bindname2]] sort.");

        assertThat(pt.getBindPoints()).containsExactly(
                new BindPoint("bindname1", "bindspec1"),
                new BindPoint("bindname2", "bindspec2")
        );
    }

    @Test
    public void shouldMatchBasicCapturePoint() {
        ParsedStringTemplate pt = new ParsedStringTemplate(
            "select [u],[v as v1] from users where userid={userid}", Map.of("userid", "NumberNameToString()")
        );
        assertThat(pt.getAnchors()).containsExactly("userid");
        assertThat(pt.getType()).isEqualTo(BindType.concat);
        assertThat(pt.getCaptures()).containsExactly(CapturePoint.of("u"),CapturePoint.of("v","v1"));

    }

}
