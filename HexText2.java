/*
  Hexadecimal Text #2 - Convert Text Characters to Hexadecimal Digits
  Written by: Keith Fenske, http://www.psc-consulting.ca/fenske/
  Thursday, 21 April 2005
  Java class name: HexText2
  Copyright (c) 2005 by Keith Fenske.  Released under GNU Public License.

  This is a graphical Java 1.4 Swing (GUI) applet or application to convert
  between text characters and decimal or hexadecimal digits representing those
  characters.  You have several options for the converted digits: decimal (base
  10) or hexadecimal (base 16), 8-bit "plain text" bytes, 16-bit combined
  words, 16-bit "big endian" bytes, 16-bit "little endian" bytes, etc.  Input
  characters that are not valid digits are ignored during conversion and are
  treated as white space or separators.  Leading zeros may be omitted unless
  the digits run together without separators.  Type or paste the characters or
  digits to be converted into the upper window, and click either the "Convert
  Text to Digits" or the "Convert Digits to Text" button.  The converted result
  will appear in the lower window.  There is a serious purpose behind these
  conversions, but it's also fun just to see what some codes represent.

  The term "big endian" means that a 16-bit number is represented by two 8-bit
  bytes with the high-order (most significant) byte appearing first.  "Little
  endian" has the low-order (least significant) byte appearing first:

              16-bit combined word:  1234 (hex)
         16-bit "big endian" bytes:  12 34
      16-bit "little endian" bytes:  34 12

  When run as an applet inside a browser on a web page, the results vary
  depending upon (1) the browser, (2) the version of Java, and (3) the system
  character set.  Keyboard copy (Ctrl-C) and paste (Ctrl-V) may only work
  inside the applet, and not with the system clipboard.  As an application,
  keyboard copy and paste work normally and there are few restrictions on the
  Unicode characters that may be entered or converted.  In other words, this
  applet is a good application but sometimes useless as an applet!  You may run
  this program as an applet on the following web page:

      Hexadecimal Text - by: Keith Fenske
      http://www.psc-consulting.ca/fenske/hextex2a.htm

  Text in Java is based on the Unicode standard, which has thousands of
  characters.  Note that most "Windows ANSI" or "Western European" characters
  in the range from 128 to 159 decimal (0x80 to 0x9F hexadecimal) have
  completely different positions in Unicode, so don't paste arbitrary 8-bit
  text and expect the converted decimal/hexadecimal to be 8-bit.  This program
  works best if you have the "Arial Unicode MS" font installed (currently with
  50,377 glyphs).  Extended Unicode characters are not supported where multiple
  16-bit values are combined into additional character codes.

  (The older AWT TextArea object allows copy and paste from the system
  clipboard.  Unfortunately, it doesn't display Unicode text well; the newer
  Swing JTextArea object is needed for that.)

  GNU General Public License (GPL)
  --------------------------------
  HexText2 is free software: you can redistribute it and/or modify it under the
  terms of the GNU General Public License as published by the Free Software
  Foundation, either version 3 of the License or (at your option) any later
  version.  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
  more details.

  You should have received a copy of the GNU General Public License along with
  this program.  If not, see the http://www.gnu.org/licenses/ web page.
*/

import java.awt.*;                // older Java GUI support
import java.awt.event.*;          // older Java GUI event support
import javax.swing.*;             // newer Java GUI support

public class HexText2
       extends JApplet
       implements ActionListener
{
  /* constants */

  static final Color BACKGROUND = new Color(255, 204, 204); // light pink

  static final String[] BASE_CHOICES = {
    "binary (2)",
    "decimal (10)",
    "hexadecimal (16)",
    "octal (8)",
    "ternary (3)"};

  static final String[] CONVERT_CHOICES = {
    "8-bit \"plain text\" bytes",
    "16-bit combined words",
    "16-bit \"big endian\" bytes",
    "16-bit \"little endian\" bytes"};

  static final int Base02Bit08Plain = 1; // give names to conversion types
  static final int Base02Bit16Combo = 2;
  static final int Base02Bit16Big = 3;
  static final int Base02Bit16Little = 4;

  static final int Base03Bit08Plain = 5;
  static final int Base03Bit16Combo = 6;
  static final int Base03Bit16Big = 7;
  static final int Base03Bit16Little = 8;

  static final int Base08Bit08Plain = 9;
  static final int Base08Bit16Combo = 10;
  static final int Base08Bit16Big = 11;
  static final int Base08Bit16Little = 12;

  static final int Base10Bit08Plain = 13;
  static final int Base10Bit16Combo = 14;
  static final int Base10Bit16Big = 15;
  static final int Base10Bit16Little = 16;

  static final int Base16Bit08Plain = 17;
  static final int Base16Bit16Combo = 18;
  static final int Base16Bit16Big = 19;
  static final int Base16Bit16Little = 20;

  static final String COPYRIGHT_NOTICE =
    "Copyright (c) 2005 by Keith Fenske.  Released under GNU Public License.";

  static final String DEFAULT_STATUS = "Converted text or digits appear above."
    + " Digits marked \"?\" are out of range.";

  static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6',  '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

  static final String PROGRAM_TITLE =
    "Convert Text Characters to Hexadecimal Digits - by: Keith Fenske";

  static final Color TEXT_COLOR = new Color(204, 255, 255); // light cyan

  /* class variables (none) */

  /* instance variables, including shared GUI components */

  JButton aboutButton;            // "About" button
  JComboBox baseList;             // numeric base/radix choices
  JButton clearButton;            // "Clear" button
  JComboBox convertList;          // conversion choices
  JButton digitsToTextButton;     // "Convert Digits to Text" button
  JTextArea inputText;            // input text from user
  JTextArea outputText;           // converted output text
  JLabel statusText;              // status message that appears at bottom
  JButton textToDigitsButton;     // "Convert Text to Digits" button


/*
  init() method

  Initialize this applet (equivalent to the main() method in an application).
  Please note the following about writing applets:

  (1) An Applet is an AWT Component just like a Button, Frame, or Panel.  It
      has a width, a height, and you can draw on it (given a proper graphical
      context, as in the paint() method).

  (2) Applets shouldn't attempt to exit, such as by calling the System.exit()
      method, because this isn't allowed on a web page.
*/
  public void init()
  {
    /* Create the graphical interface as a series of little panels inside
    bigger panels.  The intermediate panel names are of no lasting importance
    and hence are only numbered (panel1, panel2, etc). */

    /* Make an input text area.  The size in rows and columns is nominal since
    this text area will expand and contract with the window size. */

    inputText = new JTextArea(2, 20);
    inputText.setBackground(TEXT_COLOR);
    inputText.setEditable(true);  // user can change this text area
    inputText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 18));
    inputText.setLineWrap(true);  // allow input lines to wrap
    inputText.setMargin(new Insets(10, 12, 10, 12));
    inputText.setWrapStyleWord(true); // try to wrap at white space

    /* Make a list of available conversion types. */

    JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
    panel1.setBackground(BACKGROUND);

    panel1.add(new JLabel("Convert text as:", Label.RIGHT));

    baseList = new JComboBox(BASE_CHOICES); // numeric base/radix choices
    baseList.setBackground(BACKGROUND);
    baseList.setSelectedIndex(2); // default is hexadecimal
    panel1.add(baseList);

    convertList = new JComboBox(CONVERT_CHOICES); // conversion choices
    convertList.setBackground(BACKGROUND);
    convertList.setSelectedIndex(1); // default is 16-bit combined words
    panel1.add(convertList);

    /* Make a horizontal panel to hold four buttons of different sizes. */

    JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    panel2.setBackground(BACKGROUND);

    clearButton = new JButton("Clear");
    clearButton.addActionListener((ActionListener) this);
    clearButton.setBackground(BACKGROUND);
    clearButton.setToolTipText("clear input and output text areas");
    panel2.add(clearButton);

    digitsToTextButton = new JButton("Convert Digits to Text");
    digitsToTextButton.addActionListener((ActionListener) this);
    digitsToTextButton.setBackground(BACKGROUND);
    digitsToTextButton.setToolTipText("convert input hex to text output");
    panel2.add(digitsToTextButton);

    textToDigitsButton = new JButton("Convert Text to Digits");
    textToDigitsButton.addActionListener((ActionListener) this);
    textToDigitsButton.setBackground(BACKGROUND);
    textToDigitsButton.setToolTipText("convert input text to hex output");
    panel2.add(textToDigitsButton);

    aboutButton = new JButton("About");
    aboutButton.addActionListener((ActionListener) this);
    aboutButton.setBackground(BACKGROUND);
    aboutButton.setToolTipText("about this program");
    panel2.add(aboutButton);

    /* Make an output text area.  Like the input text area, it will expand and
    contract with the window size. */

    outputText = new JTextArea(2, 20);
    outputText.setBackground(TEXT_COLOR);
    outputText.setEditable(false); // user can't change this text area
    outputText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 18));
    outputText.setLineWrap(true); // allow output lines to wrap
    outputText.setMargin(new Insets(10, 12, 10, 12));
    outputText.setWrapStyleWord(true); // try to wrap at white space

    /* Our input and output text areas should expand as the window size
    changes.  Fortunately, they are symmetrical.  Create a border layout for
    the top half and another border layout for the bottom half.  Put the text
    areas in the center of the border layouts.  Then put both border layouts
    inside a grid layout, which will force them to be the same size.  Labels
    are inside flow panels to keep them aligned properly, and to add space
    around the label text. */

    JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    panel3.add(new JLabel("Enter digits or text below."));
    panel3.setBackground(BACKGROUND);

    JPanel panel4 = new JPanel(new BorderLayout(0, 0));
    panel4.add(panel3, BorderLayout.NORTH);
    panel4.add(new JScrollPane(inputText), BorderLayout.CENTER);
    panel4.add(panel1, BorderLayout.SOUTH); // conversion types
    panel4.setBackground(BACKGROUND);

    JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
    statusText = new JLabel(DEFAULT_STATUS);
    panel5.add(statusText);
    panel5.setBackground(BACKGROUND);

    JPanel panel6 = new JPanel(new BorderLayout(0, 0));
    panel6.add(panel2, BorderLayout.NORTH); // regular buttons
    panel6.add(new JScrollPane(outputText), BorderLayout.CENTER);
    panel6.add(panel5, BorderLayout.SOUTH);
    panel6.setBackground(BACKGROUND);

    JPanel panel7 = new JPanel(new GridLayout(2, 1, 0, 0));
    panel7.setBackground(BACKGROUND);
    panel7.add(panel4);
    panel7.add(panel6);

    /* Put the combined panel as this applet's window. */

    this.getContentPane().setLayout(new BorderLayout(0, 0));
    this.getContentPane().add(panel7, BorderLayout.CENTER);
    this.validate();              // do the window layout

    /* Return from init() and let the graphical interface run the applet. */

    doClearButton();              // clear input and output text areas

  } // end of init() method


/*
  main() method

  Applets only need an init() method to start execution.  This main() method is
  a wrapper that allows the same applet code to run as an application.
*/
  public static void main(String[] args)
  {
    HexText2 appletPanel;         // the target applet's window
    JFrame mainFrame;             // this application's window

    /* Create the frame that will hold the applet. */

    appletPanel = new HexText2(); // create instance of target applet

    mainFrame = new JFrame(PROGRAM_TITLE);
    mainFrame.getContentPane().setLayout(new BorderLayout(5, 5));
    mainFrame.getContentPane().add(appletPanel, BorderLayout.CENTER);

    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setLocation(50, 50); // top-left corner of application window
    mainFrame.setSize(700, 500);  // initial size of application window
    mainFrame.validate();         // do the application window layout
    mainFrame.setVisible(true);   // show the application window

    /* Initialize the applet after the layout and sizes of the main frame have
    been determined. */

    appletPanel.init();           // initialize applet with correct sizes

  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  actionPerformed() method

  This method is called when the user clicks on a control button.
*/
  public void actionPerformed(ActionEvent event)
  {
    Object source = event.getSource(); // where the event came from

    if (source == aboutButton)    // about this program
    {
      outputText.setText(PROGRAM_TITLE
        + "\n\nThis Java Swing applet converts between text characters and"
        + " decimal or hexadecimal digits representing those characters."
        + "  Results vary depending upon your browser, your version of Java,"
        + " and your system's character set.\n\n" + COPYRIGHT_NOTICE + "\n");
      outputText.requestFocusInWindow(); // move focus to our "about" text
      outputText.select(0, 0);    // force scroll to beginning
      statusText.setText(DEFAULT_STATUS); // reset any previous status message
    }
    else if (source == clearButton)
    {
      doClearButton();            // clear input and output text areas
    }
    else if (source == digitsToTextButton)
    {
      doDigitsToTextButton();     // convert input hex to text output
    }
    else if (source == textToDigitsButton)
    {
      doTextToDigitsButton();     // convert input text to hex output
    }
    else
    {
      System.out.println(
        "error in actionPerformed(): ActionEvent not recognized: " + event);
    }
  } // end of actionPerformed() method


/*
  convertDigitsToText() method

  This is a general method to convert digit strings to text characters.  White
  space may appear between complete groups of digits, or between parts in "big
  endian" and "little endian" groups.  A short list of separators are treated
  as white space.  Digits can be run together only if they have the maximum
  number of digits for each part.  Leading zeros may be omitted when digit
  groups are delimited by white space or separators.  Digit groups that exceed
  their allowed range are replaced by question marks ("?").  Missing digits may
  make the final character incomplete.

  The caller tells us:

    - the numeric base (radix): 10 for decimal, 16 for hexadecimal, etc;
    - how many parts the digits may be split into;
    - the maximum number of digits in each part;
    - if the parts are assembled in normal or reverse order; and
    - how big each part is for error checking and combining parts.

  Note one of the great secrets of writing software: by making this method more
  general, we actually simplify the code because we avoid having numerous
  special cases.  There are, of course, even more ways to generalize this
  algorithm.  We could, for example, allow the digits in each part to be in
  normal or reversed order.  However, that is beyond current requirements, and
  since we already have a single common method for all conversion options, it
  is not worthwhile adding parameters that will never be used.
*/
  void convertDigitsToText(
    int baseRadix,                // numeric base/radix: 10, 16, etc.
    int partsPerChar,             // number of parts per character: 1 or 2
    int digitsPerPart,            // maximum digits per part
    boolean reverseFlag,          // false for "big endian" order,
                                  // true for "little endian" order
    int partMaxValue,             // 0xFF for 8-bit, 0xFFFF for 16-bit
    int partShiftSize)            // 8 for 8-bit, 16 for 16-bit
  {
    int digitsFound;              // number of digits found in current part
    int digitValue;               // integer value of one digit
    String errorString;           // used to construct error message
    int i;                        // index variable
    char inputChar;               // current input character
    int inputLength;              // number of input characters
    String inputString;           // input digits from text area
    boolean invalidDigitFound;    // true if invalid input is found
    StringBuffer outputBuffer;    // generated text for output
    int outputValue;              // current integer value of output character
    boolean partRangeBad;         // true if this part is out of range
    int partsFound;               // number of parts found in current character
    int partValue;                // current integer value of current part
    boolean rangeErrorFound;      // true if any part exceeds its range

    /* Initialize by copying the input digits from the input text area. */

    inputString = inputText.getText(); // get input from text area
    inputLength = inputString.length(); // number of input characters
    outputBuffer = new StringBuffer(inputLength / partsPerChar);
                                  // set size for worst case and clear buffer
    digitsFound = outputValue = partsFound = partValue = 0;
    invalidDigitFound = partRangeBad = rangeErrorFound = false;

    /* Parse each input digit, one at a time.  Any input that is not a valid
    digit is treated as white space or a separator. */

    for (i = 0; i <= inputLength; i ++) // yes, "<=" is correct (see below)
    {
      /* Get next input character.  Supply a dummy space after the last real
      input character, to finish any unprocessed digits.  This saves us from
      having the same code duplicated after the end of the "for" loop. */

      if (i < inputLength)        // still doing real input characters?
        inputChar = inputString.charAt(i); // yes
      else
        inputChar = ' ';          // no, force separator as last character

      /* Convert input digit to binary or a separator.  We parse all possible
      digits, even those that may later be too large for the current numeric
      base.  Just for fun, we accept some extended Unicode characters. */

      switch (inputChar)
      {
        case '0':                 // zero
        case '\uff10':            // full-width '0'
          digitValue = 0;
          break;

        case '1':                 // one
        case '\uff11':            // full-width '1'
          digitValue = 1;
          break;

        case '2':
        case '\uff12':            // full-width '2'
          digitValue = 2;
          break;

        case '3':
        case '\uff13':            // full-width '3'
          digitValue = 3;
          break;

        case '4':
        case '\uff14':            // full-width '4'
          digitValue = 4;
          break;

        case '5':
        case '\uff15':            // full-width '5'
          digitValue = 5;
          break;

        case '6':
        case '\uff16':            // full-width '6'
          digitValue = 6;
          break;

        case '7':
        case '\uff17':            // full-width '7'
          digitValue = 7;
          break;

        case '8':
        case '\uff18':            // full-width '8'
          digitValue = 8;
          break;

        case '9':
        case '\uff19':            // full-width '9'
          digitValue = 9;
          break;

        case 'A':
        case 'a':
        case '\uff21':            // full-width 'A'
        case '\uff41':            // full-width 'a'
          digitValue = 10;
          break;

        case 'B':
        case 'b':
        case '\uff22':            // full-width 'B'
        case '\uff42':            // full-width 'b'
          digitValue = 11;
          break;

        case 'C':
        case 'c':
        case '\uff23':            // full-width 'C'
        case '\uff43':            // full-width 'c'
          digitValue = 12;
          break;

        case 'D':
        case 'd':
        case '\uff24':            // full-width 'D'
        case '\uff44':            // full-width 'd'
          digitValue = 13;
          break;

        case 'E':
        case 'e':
        case '\uff25':            // full-width 'E'
        case '\uff45':            // full-width 'e'
          digitValue = 14;
          break;

        case 'F':
        case 'f':
        case '\uff26':            // full-width 'F'
        case '\uff46':            // full-width 'f'
          digitValue = 15;
          break;

        case ' ':                 // list of accepted separators
        case '\0':                // null
        case '\f':                // form feed
        case '\n':                // newline
        case '\r':                // carriage return
        case '\t':                // tab
        case ',':                 // comma
        case '-':                 // hyphen
        case '.':                 // period
        case '/':                 // slash
        case ':':                 // colon
        case ';':                 // semicolon
          digitValue = -1;        // flag as a separator
          break;

        default:                  // anything else is invalid
          digitValue = -1;        // treat as a separator
          invalidDigitFound = true; // warn later about invalid input
          break;
      }

      /* Check if digit found is allowed by the current base/radix. */

      if (digitValue >= baseRadix)
      {
        digitValue = -1;          // change invalid digit into separator
        invalidDigitFound = true; // warn later about invalid input
      }

      /* Process digit, if found.  After enough digits for one part, pretend
      that we saw a separator to end the part. */

      if (digitValue >= 0)
      {
        partValue = (partValue * baseRadix) + digitValue; // add digit to part
        digitsFound ++;           // one more digit found
        if (digitsFound >= digitsPerPart) // have we finished one part?
          digitValue = -1;        // yes, create a fake separator
      }

      /* Finish part if a separator was found. */

      if (digitValue < 0)         // negative for separator (real or pretend)
      {
        if (digitsFound > 0)      // did we find any digits?
        {
          if (partValue > partMaxValue) // are the digits out of range?
          {
            partValue = 0;        // yes, don't use out-of-range value
            partRangeBad = rangeErrorFound = true; // flag this for later
          }
          if (reverseFlag)        // reversed order ("little endian")?
            outputValue += (partValue << (partShiftSize * partsFound));
          else
            outputValue = (outputValue << partShiftSize) + partValue;

          digitsFound = partValue = 0; // clear for later
          partsFound ++;          // one more part found
          if (partsFound >= partsPerChar) // enough for one output character?
          {
            if (partRangeBad)     // was there a problem with the digits?
              outputValue = '?';  // yes, substitute a question mark
            outputBuffer.append((char) outputValue); // append to output text
            outputValue = partsFound = 0; // clear for later
            partRangeBad = false; // next part starts clean
          }
        }
        else
        {
          /* Ignore multiple separators. */
        }
      }
    }

    /* If there are any warnings, put them in a status message at the bottom
    of our window. */

    errorString = "";             // assume no errors

    if (partsFound != 0)          // should reduce to zero if complete input
    {
      errorString += " Input incomplete.";
      outputBuffer.append((char) outputValue); // append unfinished part(s)
    }

    if (rangeErrorFound)          // any range errors?
      errorString += " Digits out of range.";

    if (invalidDigitFound)        // any illegal input characters?
      errorString += " Invalid digits found.";

    if (errorString.length() > 0) // any error messages?
      statusText.setText("Warning:" + errorString); // yes, insert warning
    else                          // no errors
      statusText.setText(DEFAULT_STATUS); // reset any previous status message

    /* Put generated text characters into output text area. */

    outputText.setText(outputBuffer.toString());
    outputText.requestFocusInWindow(); // move for easier Ctrl-A + copying
    outputText.select(0, 0);      // force scroll to beginning

  } // end of convertDigitsToText() method


/*
  convertTextToDigits() method

  This is a general method to convert text characters to digit strings.  It is
  easier than convertDigitsToText() because there is no parsing involved, only
  formatting.  For all numeric bases/radixes, digit strings have the maximum
  number of digits per part separated by spaces.

  The caller tells us:

    - the numeric base (radix): 10 for decimal, 16 for hexadecimal, etc;
    - how many parts the digits are split into;
    - the maximum number of digits in each part;
    - if the parts are assembled in normal or reverse order; and
    - how big each part is for splitting parts.
*/
  void convertTextToDigits(
    int baseRadix,                // numeric base/radix: 10, 16, etc.
    int partsPerChar,             // number of parts per character: 1 or 2
    int digitsPerPart,            // maximum digits per part
    boolean reverseFlag,          // false for "big endian" order,
                                  // true for "little endian" order
    int partMaxValue,             // 0xFF for 8-bit, 0xFFFF for 16-bit
    int partShiftSize)            // 8 for 8-bit, 16 for 16-bit
  {
    int charValue;                // current integer value of current character
    String charString;            // digit string for current character
    int digitValue;               // integer value of one digit
    int i, j, k;                  // index variables
    int inputLength;              // number of input characters
    String inputString;           // text characters from input
    StringBuffer outputBuffer;    // generated digits for output
    String partString;            // digit string for current part
    int partValue;                // current integer value of current part

    /* Initialize by copying the input characters from the input text area. */

    inputString = inputText.getText(); // get input from text area
    inputLength = inputString.length(); // number of input characters
    outputBuffer = new StringBuffer(inputLength * partsPerChar
      * (1 + digitsPerPart));     // set correct size and clear buffer

    /* Each input character is independent.  We only have to remember to put a
    space between digit groups generated for each character. */

    for (i = 0; i < inputLength; i ++)
    {
      charValue = inputString.charAt(i); // get one input character
      charString = "";            // start with empty digit string for this char

      /* Remove one "part" (digit group) at a time, starting at the low-order
      end of the input character. */

      for (j = 0; j < partsPerChar; j ++)
      {
        partValue = charValue & partMaxValue; // extract part from character
        charValue = charValue >> partShiftSize; // reduce char value by one part
        partString = "";          // start with empty string for this part

        /* Remove one digit from current part at the low-order end.  Digits for
        each part are always assembled into a string that has the high-order
        digit on the left (start of string) and low-order digit on the right
        (end of string).  No reverse order allowed here. */

        for (k = 0; k < digitsPerPart; k ++)
        {
          digitValue = partValue % baseRadix; // extract digit from part
          partValue = partValue / baseRadix; // reduce part value by one digit
          partString = HEX_DIGITS[digitValue] + partString; // insert digit
        }

        /* Insert digit string for current part either at the beginning or the
        end of the character string. */

        if (j > 0)                // is this the first digit group (part)?
        {
          if (reverseFlag)        // right-to-left order?
            charString = charString + ' ' + partString; // insert space
          else                    // no, left-to-right order
            charString = partString + ' ' + charString; // insert space
        }
        else
          charString = partString; // first group, just copy string
      }

      /* Append digit string for this character to end of output string. */

      if (i > 0)                  // is output string currently empty?
        outputBuffer.append(' '); // no, insert space for a separator
      outputBuffer.append(charString); // append digit string
    }

    /* Put generated text characters into output text area. */

    outputText.setText(outputBuffer.toString());
    outputText.requestFocusInWindow(); // move for easier Ctrl-A + copying
    outputText.select(0, 0);      // force scroll to beginning
    statusText.setText(DEFAULT_STATUS); // reset any previous status message

  } // end of convertTextToDigits() method


/*
  doClearButton() method

  Clear input and output text areas.
*/
  void doClearButton()
  {
    inputText.requestFocusInWindow(); // move focus so that new input goes here
    inputText.setText("");        // delete input text characters (if any)
    outputText.setText("");       // delete output text characters (if any)
    statusText.setText(DEFAULT_STATUS); // reset any previous status message
  }


/*
  doDigitsToTextButton() method

  Convert input digits to output text.  These are special cases of one common
  routine.  The parameters here should be identical to the parameters in the
  doTextToDigitsButton() method.
*/
  void doDigitsToTextButton()
  {
    switch (getConversionType())
    {
      case Base02Bit08Plain:      // binary 8-bit "plain text" bytes
        convertDigitsToText(2, 1, 8, false, 0xFF, 8);
                                  // base 2, 1 part, 8 digits
        break;

      case Base02Bit16Combo:      // binary 16-bit combined words
        convertDigitsToText(2, 1, 16, false, 0xFFFF, 16);
                                  // base 2, 1 part, 16 digits
        break;

      case Base02Bit16Big:        // binary 16-bit "big endian" bytes
        convertDigitsToText(2, 2, 8, false, 0xFF, 8);
                                  // base 2, 2 parts, 8 digits, normal order
        break;

      case Base02Bit16Little:     // binary 16-bit "little endian" bytes
        convertDigitsToText(2, 2, 8, true, 0xFF, 8);
                                  // base 2, 2 parts, 8 digits, reversed
        break;

      case Base03Bit08Plain:      // ternary 8-bit "plain text" bytes
        convertDigitsToText(3, 1, 6, false, 0xFF, 8);
                                  // base 3, 1 part, 6 digits
        break;

      case Base03Bit16Combo:      // ternary 16-bit combined words
        convertDigitsToText(3, 1, 11, false, 0xFFFF, 16);
                                  // base 3, 1 part, 11 digits
        break;

      case Base03Bit16Big:        // ternary 16-bit "big endian" bytes
        convertDigitsToText(3, 2, 6, false, 0xFF, 8);
                                  // base 3, 2 parts, 6 digits, normal order
        break;

      case Base03Bit16Little:     // ternary 16-bit "little endian" bytes
        convertDigitsToText(3, 2, 6, true, 0xFF, 8);
                                  // base 3, 2 parts, 6 digits, reversed
        break;

      case Base08Bit08Plain:      // octal 8-bit "plain text" bytes
        convertDigitsToText(8, 1, 3, false, 0xFF, 8);
                                  // base 8, 1 part, 3 digits
        break;

      case Base08Bit16Combo:      // octal 16-bit combined words
        convertDigitsToText(8, 1, 6, false, 0xFFFF, 16);
                                  // base 8, 1 part, 6 digits
        break;

      case Base08Bit16Big:        // octal 16-bit "big endian" bytes
        convertDigitsToText(8, 2, 3, false, 0xFF, 8);
                                  // base 8, 2 parts, 3 digits, normal order
        break;

      case Base08Bit16Little:     // octal 16-bit "little endian" bytes
        convertDigitsToText(8, 2, 3, true, 0xFF, 8);
                                  // base 8, 2 parts, 3 digits, reversed
        break;

      case Base10Bit08Plain:      // decimal 8-bit "plain text" bytes
        convertDigitsToText(10, 1, 3, false, 0xFF, 8);
                                  // base 10, 1 part, 3 digits
        break;

      case Base10Bit16Combo:      // decimal 16-bit combined words
        convertDigitsToText(10, 1, 5, false, 0xFFFF, 16);
                                  // base 10, 1 part, 5 digits
        break;

      case Base10Bit16Big:        // decimal 16-bit "big endian" bytes
        convertDigitsToText(10, 2, 3, false, 0xFF, 8);
                                  // base 10, 2 parts, 3 digits, normal order
        break;

      case Base10Bit16Little:     // decimal 16-bit "little endian" bytes
        convertDigitsToText(10, 2, 3, true, 0xFF, 8);
                                  // base 10, 2 parts, 3 digits, reversed
        break;

      case Base16Bit08Plain:      // hex 8-bit "plain text" bytes
        convertDigitsToText(16, 1, 2, false, 0xFF, 8);
                                  // base 16, 1 part, 2 digits
        break;

      case Base16Bit16Combo:      // hex 16-bit combined words
        convertDigitsToText(16, 1, 4, false, 0xFFFF, 16);
                                  // base 16, 1 part, 4 digits
        break;

      case Base16Bit16Big:        // hex 16-bit "big endian" bytes
        convertDigitsToText(16, 2, 2, false, 0xFF, 8);
                                  // base 16, 2 parts, 2 digits, normal order
        break;

      case Base16Bit16Little:     // hex 16-bit "little endian" bytes
        convertDigitsToText(16, 2, 2, true, 0xFF, 8);
                                  // base 16, 2 parts, 2 digits, reversed
        break;

      default:
        System.out.println("error in doDigitsToTextButton(): getConversionType() = "
          + getConversionType());
        outputText.setText("Internal error in doDigitsToTextButton() method.");
        break;
    }
  } // end of doDigitsToTextButton() method


/*
  doTextToDigitsButton() method

  Convert input text to output digits.  These are special cases of one common
  routine.  The parameters here should be identical to the parameters in the
  doDigitsToTextButton() method.
*/
  void doTextToDigitsButton()
  {
    switch (getConversionType())
    {
      case Base02Bit08Plain:      // binary 8-bit "plain text" bytes
        convertTextToDigits(2, 1, 8, false, 0xFF, 8);
                                  // base 2, 1 part, 8 digits
        break;

      case Base02Bit16Combo:      // binary 16-bit combined words
        convertTextToDigits(2, 1, 16, false, 0xFFFF, 16);
                                  // base 2, 1 part, 16 digits
        break;

      case Base02Bit16Big:        // binary 16-bit "big endian" bytes
        convertTextToDigits(2, 2, 8, false, 0xFF, 8);
                                  // base 2, 2 parts, 8 digits, normal order
        break;

      case Base02Bit16Little:     // binary 16-bit "little endian" bytes
        convertTextToDigits(2, 2, 8, true, 0xFF, 8);
                                  // base 2, 2 parts, 8 digits, reversed
        break;

      case Base03Bit08Plain:      // ternary 8-bit "plain text" bytes
        convertTextToDigits(3, 1, 6, false, 0xFF, 8);
                                  // base 3, 1 part, 6 digits
        break;

      case Base03Bit16Combo:      // ternary 16-bit combined words
        convertTextToDigits(3, 1, 11, false, 0xFFFF, 16);
                                  // base 3, 1 part, 11 digits
        break;

      case Base03Bit16Big:        // ternary 16-bit "big endian" bytes
        convertTextToDigits(3, 2, 6, false, 0xFF, 8);
                                  // base 3, 2 parts, 6 digits, normal order
        break;

      case Base03Bit16Little:     // ternary 16-bit "little endian" bytes
        convertTextToDigits(3, 2, 6, true, 0xFF, 8);
                                  // base 3, 2 parts, 6 digits, reversed
        break;

      case Base08Bit08Plain:      // octal 8-bit "plain text" bytes
        convertTextToDigits(8, 1, 3, false, 0xFF, 8);
                                  // base 8, 1 part, 3 digits
        break;

      case Base08Bit16Combo:      // octal 16-bit combined words
        convertTextToDigits(8, 1, 6, false, 0xFFFF, 16);
                                  // base 8, 1 part, 6 digits
        break;

      case Base08Bit16Big:        // octal 16-bit "big endian" bytes
        convertTextToDigits(8, 2, 3, false, 0xFF, 8);
                                  // base 8, 2 parts, 3 digits, normal order
        break;

      case Base08Bit16Little:     // octal 16-bit "little endian" bytes
        convertTextToDigits(8, 2, 3, true, 0xFF, 8);
                                  // base 8, 2 parts, 3 digits, reversed
        break;

      case Base10Bit08Plain:      // decimal 8-bit "plain text" bytes
        convertTextToDigits(10, 1, 3, false, 0xFF, 8);
                                  // base 10, 1 part, 3 digits
        break;

      case Base10Bit16Combo:      // decimal 16-bit combined words
        convertTextToDigits(10, 1, 5, false, 0xFFFF, 16);
                                  // base 10, 1 part, 5 digits
        break;

      case Base10Bit16Big:        // decimal 16-bit "big endian" bytes
        convertTextToDigits(10, 2, 3, false, 0xFF, 8);
                                  // base 10, 2 parts, 3 digits, normal order
        break;

      case Base10Bit16Little:     // decimal 16-bit "little endian" bytes
        convertTextToDigits(10, 2, 3, true, 0xFF, 8);
                                  // base 10, 2 parts, 3 digits, reversed
        break;

      case Base16Bit08Plain:      // hex 8-bit "plain text" bytes
        convertTextToDigits(16, 1, 2, false, 0xFF, 8);
                                  // base 16, 1 part, 2 digits
        break;

      case Base16Bit16Combo:      // hex 16-bit combined words
        convertTextToDigits(16, 1, 4, false, 0xFFFF, 16);
                                  // base 16, 1 part, 4 digits
        break;

      case Base16Bit16Big:        // hex 16-bit "big endian" bytes
        convertTextToDigits(16, 2, 2, false, 0xFF, 8);
                                  // base 16, 2 parts, 2 digits, normal order
        break;

      case Base16Bit16Little:     // hex 16-bit "little endian" bytes
        convertTextToDigits(16, 2, 2, true, 0xFF, 8);
                                  // base 16, 2 parts, 2 digits, reversed
        break;

      default:
        System.out.println("error in doTextToDigitsButton(): getConversionType() = "
          + getConversionType());
        outputText.setText("Internal error in doTextToDigitsButton() method.");
        break;
    }
  } // end of doTextToDigitsButton() method


/*
  getConversionType() method

  The user selects a conversion type from drop-down lists, and Java provides
  us with indexes of the selected entries.  This method takes the indexes and
  returns a named value to indicate the correct conversion type.  This is done
  once in this routine only, because it is prone to errors if both the lists
  and this routine aren't changed at the same time.
*/
  int getConversionType()
  {
    final int[][] choiceMask = {
      {Base02Bit08Plain, Base02Bit16Combo, Base02Bit16Big, Base02Bit16Little},
      {Base10Bit08Plain, Base10Bit16Combo, Base10Bit16Big, Base10Bit16Little},
      {Base16Bit08Plain, Base16Bit16Combo, Base16Bit16Big, Base16Bit16Little},
      {Base08Bit08Plain, Base08Bit16Combo, Base08Bit16Big, Base08Bit16Little},
      {Base03Bit08Plain, Base03Bit16Combo, Base03Bit16Big, Base03Bit16Little}};

      return (choiceMask[baseList.getSelectedIndex()]
        [convertList.getSelectedIndex()]);

  } // end of getConversionType() method

} // end of HexText2 class

/* Copyright (c) 2005 by Keith Fenske.  Released under GNU Public License. */
