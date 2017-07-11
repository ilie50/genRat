<Persons>
	<Message>FreeMarker Template build XML: ${name}</Message>
	
	<#list personDetails as person>
		<Person>	
			<Name>${person.name}</Name>
			<Location>${person.location}</Location>
		</Person>
	</#list>
</Persons>