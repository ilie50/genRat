package org.genrat.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;

public interface IGeneratorService {

	ByteArrayOutputStream createPdf(InputStream templateInput, Serializable data);
}
