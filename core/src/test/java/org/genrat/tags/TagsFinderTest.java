package org.genrat.tags;

import static org.genrat.tags.TagsFinder.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.genrat.fm.FreeMarkerProcessService;
import org.genrat.fm.FreeMarkerTemplateData;
import org.genrat.tags.model.Tag;
import org.junit.Test;

public class TagsFinderTest {

	@Test
	public void testTagsFinder() {
		final Pattern pattern = Pattern.compile(EXPRESSION_TAG);
		final Matcher matcher = pattern.matcher("${String I want to extract}");
		boolean found = matcher.find();
		System.out.println(found);
		System.out.println(matcher.group(0));

		final Pattern pattern2 = Pattern.compile(DIRECTIVE_START_TAG);
		final Matcher matcher2 = pattern2.matcher("<#list personDetails as person>");
		boolean found2 = matcher2.find();
		System.out.println(found2);
		System.out.println(matcher2.group(0));

		final Pattern pattern5 = Pattern.compile(DIRECTIVE_START_TAG);
		final Matcher matcher5 = pattern5.matcher("<#else>");
		boolean found5 = matcher5.find();
		System.out.println("Start else:" + found5);
		//System.out.println(matcher5.group(0));

		
		final Pattern pattern3 = Pattern.compile(DIRECTIVE_CLAUSE_TAG);
		final Matcher matcher3 = pattern3.matcher("<#else>");
		boolean found3 = matcher3.find();
		System.out.println(found3);
		System.out.println(matcher3.group(0));

		final Pattern pattern4 = Pattern.compile(DIRECTIVE_END_TAG);
		final Matcher matcher4 = pattern4.matcher("</#list>");
		boolean found4 = matcher4.find();
		System.out.println(found4);
		System.out.println(matcher4.group(0));
		
		
		List<Tag> tagValues = new TagsFinder().getTagValues("Text1 ${name} dfddas ${newName} fgsdf <#list personDetails as person> sdfadf dd ${person.name} dfdfgsdf dfsf ${person.location} sdfdf sdfsdf </#list>");
		tagValues.forEach(s -> {
			System.out.println(s.getValue());
		});
		FreeMarkerTemplateData fmtd = new FreeMarkerTemplateData();
		String process = new FreeMarkerProcessService().process(tagValues.stream().map(tag -> tag.getValue()).reduce((s1, s2) -> s1 + "\n" + s2).get(), fmtd.getTemplateData());
		System.out.println(process);
	}


}
