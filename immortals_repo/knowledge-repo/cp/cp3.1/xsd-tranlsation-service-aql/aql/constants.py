XSL_GENERATION_EXTRA_COMMENT = """
  Description:
     Stylesheet that generates XHTML documentation, given an XML
     Schema document
  Assumptions:
     -Resulting documentation will only be displayed properly with
      the latest browsers that support XHTML and CSS. Older
      browsers are not supported.
     -Assumed that XSD document conforms to the XSD recommendation.
      No validity checking is done.
  Constraints:
     -Local schema components cannot contain two dashes in
      'documentation' elements within their 'annotation' element.
      This is because the contents of those 'documentation'
      elements are displayed in a separate window using Javascript.
      This Javascript code is enclosed in comments, which do not
      allow two dashes inside themselves.
  Notes:
     -Javascript code is placed within comments, even though in
      strict XHTML, JavaScript code should be placed within CDATA
      sections. This is because current browsers generate a syntax
      error if the page contains CDATA sections. Placing Javascript
      code within comments means that the code cannot contain two
      dashes.
      (See 'PrintJSCode' named template.)
"""

HTML_NS = "http://www.w3.org/1999/xhtml"
XSL_NS = "http://www.w3.org/1999/XSL/Transform"
XSI_NS = "http://www.w3.org/2001/XMLSchema-instance"
