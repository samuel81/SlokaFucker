package me.samuel81.jin.module.su;

import java.io.File;
import java.util.List;
import me.samuel81.jin.PDFHandler;
import me.samuel81.jin.module.Converter;

public class SUConverter extends Converter {

	public SUConverter(List<File> files) {
		super(files);
	}

	@Override
	public void convertPDF(File f) throws Exception {
		PDFHandler.processImage(f);
	}

}
