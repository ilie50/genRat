package org.genrat.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.genrat.tags.model.Tag;

public class TagsFinder {

	protected static final String EXPRESSION_TAG = "\\$\\{(.+?)\\}";
	protected static final String DIRECTIVE_START_TAG = "<#((?!else).+?)>";
	protected static final String DIRECTIVE_CLAUSE_TAG = "<#els(.+?)>";
	protected static final String DIRECTIVE_END_TAG = "</#(.+?)>";

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
}
