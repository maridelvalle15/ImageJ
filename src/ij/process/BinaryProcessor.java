package ij.process;

/** This class processes binary images. */
public class BinaryProcessor extends ByteProcessor {

	private ByteProcessor parent;
	private int foreground;
	
	/** Creates a BinaryProcessor from a ByteProcessor. The ByteProcessor
		must contain a binary image (pixels values are either 0 or 255).
		Backgound is assumed to be white. */
	public BinaryProcessor(ByteProcessor ip) {
		super(ip.getWidth(), ip.getHeight(), (byte[])ip.getPixels(), ip.getColorModel());
		setRoi(ip.getRoi());
		parent = ip;
	}

	static final int OUTLINE=0;
	
	void process(int type) {
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		int bgColor = 255;
		int offset, v=0;
        int rowOffset = width;

		if (parent.isInvertedLut())
			bgColor = 0;

		byte[] pixels2 = (byte[])parent.getPixelsCopy();
		
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			p2 = pixels2[offset-rowOffset-1]&0xff;
			p3 = pixels2[offset-rowOffset]&0xff;
			p5 = pixels2[offset-1]&0xff;
			p6 = pixels2[offset]&0xff;
			p8 = pixels2[offset+rowOffset-1]&0xff;
			p9 = pixels2[offset+rowOffset]&0xff;

			for (int x=xMin; x<=xMax; x++) {
				p1 = p2; p2 = p3;
				p3 = pixels2[offset-rowOffset+1]&0xff;
				p4 = p5; p5 = p6;
				p6 = pixels2[offset+1]&0xff;
				p7 = p8; p8 = p9;
				p9 = pixels2[offset+rowOffset+1]&0xff;

				switch (type) {
					case OUTLINE:
						v = p5;
						if (v!=bgColor) {
							if (!(p1==bgColor || p2==bgColor || p3==bgColor || p4==bgColor
								|| p6==bgColor || p7==bgColor || p8==bgColor || p9==bgColor))
									v = bgColor;
						}
						break;
				}
				
				pixels[offset++] = (byte)v;
			}
		}
	}

	// 2012/09/16: 3,0 1->0
	// 2012/09/16: 24,0 2->0
	private static int[] table  =
		//0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
		 {0,0,0,0,0,0,1,3,0,0,3,1,1,0,1,3,0,0,0,0,0,0,0,0,0,0,2,0,3,0,3,3,
		  0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,3,0,2,2,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,3,0,0,0,0,0,0,0,3,0,0,0,3,0,2,0,
		  0,0,3,1,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,
		  3,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,1,3,0,0,1,3,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  2,3,0,1,0,0,0,1,0,0,0,0,0,0,0,0,3,3,0,1,0,0,0,0,2,2,0,0,2,0,0,0};
		  
	// 2013/12/02: 16,6 2->0
	// 2013/12/02: 24,5 0->2
	private static int[] table2  =
		  //0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1
		 {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,2,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,2,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,
		  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
		  0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};


	/** Converts objects in a binary image with pixel values of
		'forground' (255 or 0) to single pixel skeletons.
		Uses a lookup table to repeatably removes pixels from the
		edges of objects in a binary image, reducing them to single
		pixel wide skeletons. There is an entry in the table for each
		of the 256 possible 3x3 neighborhood configurations. An entry
		of '1' means delete pixel on first pass, '2' means delete pixel on
		second pass, and '3' means delete on either pass. Pixels are
		removed from the right and bottom edges of objects on the first
		pass and from the left and top edges on the second pass. A
		graphical representation of the 256 neighborhoods indexed by
		the table is available at
		"http://imagej.nih.gov/ij/images/skeletonize-table.gif".
	*/
	public void skeletonize(int foreground) {
		if (!(foreground==255||foreground==0))
			throw new IllegalArgumentException("Skeletonize: foreground must be 255 or 0");
		this.foreground = foreground;
		boolean edgePixels = hasEdgePixels();
		BinaryProcessor ip2 = expand(edgePixels);
		ip2.skeletonize2(foreground);
		shrink(ip2, edgePixels);
	}

	/** Converts black objects in a binary image to single pixel skeletons. */
	 public void skeletonize() {
		int fg = parent.isInvertedLut()?255:0;
	 	skeletonize(fg);
    }

	private void  skeletonize2(int foreground) {
	 	this.foreground = foreground;
		int pass = 0;
		int pixelsRemoved;
		resetRoi();
		int background = 255 - foreground;
		setColor(background);
		moveTo(0,0); lineTo(0,height-1);
		moveTo(0,0); lineTo(width-1,0);
		moveTo(width-1,0); lineTo(width-1,height-1);
		moveTo(0,height-1); lineTo(width/*-1*/,height-1);
		ij.ImageStack movie=null;
		boolean debug = ij.IJ.debugMode;
		if (debug) movie = new ij.ImageStack(width, height);
		if (debug) movie.addSlice("-", duplicate());
		do {
			snapshot();
			pixelsRemoved = thin(pass++, table);
			if (debug) movie.addSlice(""+(pass-1), duplicate());
			snapshot();
			pixelsRemoved += thin(pass++, table);
			if (debug) movie.addSlice(""+(pass-1), duplicate());
		} while (pixelsRemoved>0);
		do { // use a second table to remove "stuck" pixels
			snapshot();
			pixelsRemoved = thin(pass++, table2);
			if (debug) movie.addSlice("2-"+(pass-1), duplicate());
			snapshot();
			pixelsRemoved += thin(pass++, table2);
			if (debug) movie.addSlice("2-"+(pass-1), duplicate());
		} while (pixelsRemoved>0);
		if (debug) new ij.ImagePlus("Skel Movie", movie).show();
	}

    private boolean hasEdgePixels() {
        int width = getWidth();
        int height = getHeight();
        boolean edgePixels = false;
        for (int x=0; x<width; x++) { // top edge
            if (getPixel(x, 0)==foreground)
                edgePixels = true;
        }
        for (int x=0; x<width; x++) { // bottom edge
            if (getPixel(x, height-1)==foreground)
                edgePixels = true;
        }
        for (int y=0; y<height; y++) { // left edge
            if (getPixel(0, y)==foreground)
                edgePixels = true;
        }
        for (int y=0; y<height; y++) { // right edge
            if (getPixel(width-1, y)==foreground)
                edgePixels = true;
        }
        return edgePixels;
    }
    
    private BinaryProcessor expand(boolean hasEdgePixels) {
        if (hasEdgePixels) {
            ByteProcessor ip2 = (ByteProcessor)createProcessor(getWidth()+2, getHeight()+2);
            BinaryProcessor bp = new BinaryProcessor(ip2);
            if (foreground==0) {
                bp.setColor(255);
                bp.fill();
            }
            bp.insert(this, 1, 1);
            return bp;
        } else
            return this;
    }

    private void shrink(ImageProcessor ip2, boolean hasEdgePixels) {
        if (hasEdgePixels) {
            int width = getWidth();
            int height = getHeight();
            for (int y=0; y<height; y++)
                for (int x=0; x<width; x++)
                    putPixel(x, y, ip2.getPixel(x+1, y+1));
        }
    }

	int thin(int pass, int[] table) {
		int bgColor = foreground==255?0:-1;
		int p1, p2, p3, p4, p5, p6, p7, p8, p9;
		byte[] pixels2 = (byte[])getPixelsCopy();
		int v, index, code;
        int offset, rowOffset = width;
        int pixelsRemoved = 0;
		for (int y=yMin; y<=yMax; y++) {
			offset = xMin + y * width;
			for (int x=xMin; x<=xMax; x++) {
				p5 = pixels2[offset];
				v = p5;
				if (v!=bgColor) {
					p1 = pixels2[offset-rowOffset-1];
					p2 = pixels2[offset-rowOffset];
					p3 = pixels2[offset-rowOffset+1];
					p4 = pixels2[offset-1];
					p6 = pixels2[offset+1];
					p7 = pixels2[offset+rowOffset-1];
					p8 = pixels2[offset+rowOffset];
					p9 = pixels2[offset+rowOffset+1];
					
					int [] p = {0,p1,p2,p3,p4,0,p6,p7,p8,p9};
					index = reassignIndex(p,bgColor);

					code = table[index];
					if ((pass&1)==1) { //odd pass
						if (code==2||code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					} else { //even pass
						if (code==1||code==3) {
							v = bgColor;
							pixelsRemoved++;
						}
					}
				}
				pixels[offset++] = (byte)v;
			}
		}
		return pixelsRemoved;
	}
	
	private int reassignIndex(int [] p, int bgColor) {
		int index = 0;
		if (p[1]!=bgColor) index |= 1;
		if (p[2]!=bgColor) index |= 2;
		if (p[3]!=bgColor) index |= 4;
		if (p[6]!=bgColor) index |= 8;
		if (p[9]!=bgColor) index |= 16;
		if (p[8]!=bgColor) index |= 32;
		if (p[7]!=bgColor) index |= 64;
		if (p[4]!=bgColor) index |= 128;

		return index;
	}

	public void outline() {
		process(OUTLINE);
	}
	
}
