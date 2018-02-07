package org.genrat.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.genrat.fm.FreeMarkerGenrateXMLFromObject;
import org.genrat.tags.Tag;

public class TagsFinder {

	private static final String EXPRESSION_TAG = "\\$\\{(.+?)\\}";
	private static final String DIRECTIVE_START_TAG = "<#((?!else).+?)>";
	private static final String DIRECTIVE_CLAUSE_TAG = "<#els(.+?)>";
	private static final String DIRECTIVE_END_TAG = "</#(.+?)>";

	private static final String TAG_REGEX = "(\\$\\{(.+?)\\})|(<#(.+?)>)|(</#(.+?)>)";

	public boolean isTag(String text) {
		return isExpressionTag(text) || isDirectiveStartTag(text) || isDirectiveEndTag(text);
	}

	public boolean isExpressionTag(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		final Pattern pattern = Pattern.compile(EXPRESSION_TAG);
		final Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	public boolean isDirectiveStartTag(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		final Pattern pattern = Pattern.compile(DIRECTIVE_START_TAG);
		final Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}

	public boolean isDirectiveClauseTag(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		final Pattern pattern = Pattern.compile(DIRECTIVE_CLAUSE_TAG);
		final Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}
	
	public boolean isDirectiveEndTag(String text) {
		if (StringUtils.isBlank(text)) {
			return false;
		}
		final Pattern pattern = Pattern.compile(DIRECTIVE_END_TAG);
		final Matcher matcher = pattern.matcher(text);
		return matcher.find();
	}


	public List<Tag> getTagValues(final String str) {
	    final List<Tag> tags = new ArrayList<>();
	    final Matcher matcher = Pattern.compile(TAG_REGEX).matcher(str);
	    while (matcher.find()) {
	    	int start = matcher.start();
			int end = matcher.end();
			tags.add(new Tag(matcher.group(0), start, end));
	    }
	    return tags;
	}
	
	public String getContent(String patternText, String text) {
		final Pattern pattern = Pattern.compile(patternText + "(.+?)" + patternText);
		final Matcher matcher = pattern.matcher(text);
		boolean found = matcher.find();
		return found ? matcher.group(1) : "";

	}
	
	public static void main(String[] args) {
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
		
		String process = new FreeMarkerGenrateXMLFromObject().process(tagValues.stream().map(tag -> tag.getValue()).reduce((s1, s2) -> s1 + "\n" + s2).get());
		System.out.println(process);
	}

}
