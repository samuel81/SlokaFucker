package me.samuel81.jin.module.bt;

import java.io.File;
import java.util.List;
import me.samuel81.jin.PDFHandler;
import me.samuel81.jin.module.Converter;

public class BTConverter extends Converter {

	public BTConverter(List<File> files) {
		super(files);
	}

	@Override
	public void convertPDF(File f) throws Exception {
		PDFHandler.compress(f);
	}

}
