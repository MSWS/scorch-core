package com.scorch.core.pastebin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Paste API
 * 
 * <br>
 * <br>
 * 
 * Before use the API define your pastebin DevKey there: {@link Paste#setDeveloperKey(String)}
 * 
 * <br>
 * <br>
 * 
 * GitHub Repo: https://github.com/rodel77/PasteBin-Java-API
 * 
 * @author rodel77
 */
public class Paste {
	private final static String POST_URL = "http://pastebin.com/api/api_post.php";
	private final static String USER_AGENT = "Mozilla/5.0";
	private static String developer_key = "Set your key there!";
	
	private String code = "";
	private String fileName = "";
	private Expire expire;
	private Visibility visibility;
	private Language language;
	
	/**
	 * Create new paste
	 * 
	 * @param code Text inside the paste
	 * @param fileName Paste text
	 * @param visibility Paste visibility
	 * @param expire Paste expire time
	 * @param language Paste language
	 */
	public Paste(String code, String fileName, Visibility visibility, Expire expire, Language language) {
		this.code = code;
		this.fileName = fileName;
		this.visibility = visibility;
		this.expire = expire;
		this.language = language;
	}

	/**
	 * Upload the paste with all info into pastebin.com
	 * 
	 * @return {@link PasteResult}
	 * @throws IOException
	 * @see {@link PasteResult}
	 */
	public PasteResult upload() throws IOException{
		String response = "";
		URL url = new URL(POST_URL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		
		conn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(getURLParameters());
		wr.flush();
		wr.close();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while((line = in.readLine()) != null){
			response+=line;
		}
		in.close();
		if(response.contains("pastebin.com")){
			return new PasteResult("", response, true);
		}
		return new PasteResult(response, "", false);
	}
	
	/**
	 * Get final url parameters
	 * 
	 *  <br>
	 *  
	 *  POST URL: http://pastebin.com/api/api_post.php?{@link Paste#getURLParameters()}
	 * 
	 * @return
	 */
	public String getURLParameters(){
		String param = "api_option=paste"
			 + "&api_dev_key="+developer_key+""
			 + "&api_paste_private="+visibility.id+""
			 + "&api_paste_name="+fileName+""
			 + "&api_paste_format="+language.apiName+""
			 + "&api_paste_code="+code;
		
		if(expire!=Expire.NEVER){
			param+="&api_expire_date="+expire.apiName;
		}
		
		return param;
	}
	
	/**
	 * Set paste format
	 * 
	 * @return
	 */
	public Language getLanguage(){
		return language;
	}
	
	/**
	 * Set paste format
	 * 
	 * @param language
	 */
	public void setLanguage(Language language){
		this.language = language;
	}
	
	/**
	 * Set visibility
	 * 
	 * @return
	 */
	public Visibility getVisibility(){
		return visibility;
	}
	
	/**
	 * Set visibility
	 * 
	 * @param visibility
	 */
	public void setVisibility(Visibility visibility){
		this.visibility = visibility;
	}
	
	/**
	 * Get expire time
	 * 
	 * @return
	 */
	public Expire getExpire(){
		return expire;
	}
	
	
	/**
	 * Set expire time
	 * 
	 * @param expire
	 */
	public void setExpire(Expire expire){
		this.expire = expire;
	}
	
	/**
	 * Get the paste name
	 * 
	 * @return
	 */
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * Set the paste name
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Get the paste
	 * 
	 * @return
	 */
	public String getCode(){
		return code;
	}
	
	/**
	 * Set the text in your paste
	 * 
	 * <br>
	 * 
	 * Use: \n for line break
	 * 
	 * @param code
	 */
	public void setCode(String code){
		this.code = code;
	}
	
	/**
	 * Set the text in your paste
	 * 
	 * @param code File to paste
	 * @throws IOException
	 */
	public void setCode(File code) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(code));
		String ln;
		while((ln = reader.readLine()) != null){
			this.code+=ln+"\n";
		}
		reader.close();
	}
	
	/**
	 * Set your static developer key
	 * 
	 * @param developerKey Developer key generated there: http://pastebin.com/api (You have to login and then copy the text on "Your Unique Developer API Key")
	 */
	public static void setDeveloperKey(String developerKey){
		developer_key = developerKey;
	}
	
	/**
	 * Paste Language 
	 * @author rodel77
	 */
	public enum Language{
		CS("4cs"),
		ACME("6502acme"),
		KICKASS("6502kickass"),
		TASM("6502tasm"),
		ABAP("abap"),
		ACTIONSCRIPT("actionscript"),
		ACTIONSCRIPT3("actionscript3"),
		ADA("ada"),
		AIMMS("aimms"),
		ALGOL68("algol68"),
		APACHE("apache"),
		APPLESCRIPT("applescript"),
		APT_SOURCES("apt_sources"),
		ARM("arm"),
		ASM("asm"),
		ASP("asp"),
		ASYMPTOTE("asymptote"),
		AUTOCONF("autoconf"),
		AUTOHOTKEY("autohotkey"),
		AUTOIT("autoit"),
		AVISYNTH("avisynth"),
		AWK("awk"),
		BASCOMAVR("bascomavr"),
		BASH("bash"),
		BASIC4GL("basic4gl"),
		DOS("dos"),
		BIBTEX("bibtex"),
		BLITZBASIC("blitzbasic"),
		B3D("b3d"),
		BMX("bmx"),
		BNF("bnf"),
		BOO("boo"),
		BF("bf"),
		C("c"),
		C_WINAPI("c_winapi"),
		C_MAC("c_mac"),
		CIL("cil"),
		CSHARP("csharp"),
		CPP("cpp"),
		CPPWINAPI("cpp-winapi"),
		CPPQT("cpp-qt"),
		C_LOADRUNNER("c_loadrunner"),
		CADDCL("caddcl"),
		CADLISP("cadlisp"),
		CEYLON("ceylon"),
		CFDG("cfdg"),
		CHAISCRIPT("chaiscript"),
		CHAPEL("chapel"),
		CLOJURE("clojure"),
		KLONEC("klonec"),
		KLONECPP("klonecpp"),
		CMAKE("cmake"),
		COBOL("cobol"),
		COFFEESCRIPT("coffeescript"),
		CFM("cfm"),
		CSS("css"),
		CUESHEET("cuesheet"),
		D("d"),
		DART("dart"),
		DCL("dcl"),
		DCPU16("dcpu16"),
		DCS("dcs"),
		DELPHI("delphi"),
		OXYGENE("oxygene"),
		DIFF("diff"),
		DIV("div"),
		DOT("dot"),
		E("e"),
		EZT("ezt"),
		ECMASCRIPT("ecmascript"),
		EIFFEL("eiffel"),
		EMAIL("email"),
		EPC("epc"),
		ERLANG("erlang"),
		EUPHORIA("euphoria"),
		FSHARP("fsharp"),
		FALCON("falcon"),
		FILEMAKER("filemaker"),
		FO("fo"),
		F1("f1"),
		FORTRAN("fortran"),
		FREEBASIC("freebasic"),
		FREESWITCH("freeswitch"),
		GAMBAS("gambas"),
		GML("gml"),
		GDB("gdb"),
		GENERO("genero"),
		GENIE("genie"),
		GETTEXT("gettext"),
		GO("go"),
		GROOVY("groovy"),
		GWBASIC("gwbasic"),
		HASKELL("haskell"),
		HAXE("haxe"),
		HICEST("hicest"),
		HQ9PLUS("hq9plus"),
		HTML4STRICT("html4strict"),
		HTML5("html5"),
		ICON("icon"),
		IDL("idl"),
		INI("ini"),
		INNO("inno"),
		INTERCAL("intercal"),
		IO("io"),
		ISPFPANEL("ispfpanel"),
		J("j"),
		JAVA("java"),
		JAVA5("java5"),
		JAVASCRIPT("javascript"),
		JCL("jcl"),
		JQUERY("jquery"),
		JSON("json"),
		JULIA("julia"),
		KIXTART("kixtart"),
		KOTLIN("kotlin"),
		LATEX("latex"),
		LDIF("ldif"),
		LB("lb"),
		LSL2("lsl2"),
		LISP("lisp"),
		LLVM("llvm"),
		LOCOBASIC("locobasic"),
		LOGTALK("logtalk"),
		LOLCODE("lolcode"),
		LOTUSFORMULAS("lotusformulas"),
		LOTUSSCRIPT("lotusscript"),
		LSCRIPT("lscript"),
		LUA("lua"),
		M68K("m68k"),
		MAGIKSF("magiksf"),
		MAKE("make"),
		MAPBASIC("mapbasic"),
		MARKDOWN("markdown"),
		MATLAB("matlab"),
		MIRC("mirc"),
		MMIX("mmix"),
		MODULA2("modula2"),
		MODULA3("modula3"),
		EVPAC("68000devpac"),
		MPASM("mpasm"),
		MXML("mxml"),
		MYSQL("mysql"),
		NAGIOS("nagios"),
		NETREXX("netrexx"),
		NEWLISP("newlisp"),
		NGINX("nginx"),
		NIMROD("nimrod"),
		TEXT("text"),
		NSIS("nsis"),
		OBERON2("oberon2"),
		OBJECK("objeck"),
		OBJC("objc"),
		OCAMLBRIEF("ocaml-brief"),
		OCAML("ocaml"),
		OCTAVE("octave"),
		OOREXX("oorexx"),
		PF("pf"),
		GLSL("glsl"),
		OOBAS("oobas"),
		ORACLE11("oracle11"),
		ORACLE8("oracle8"),
		OZ("oz"),
		PARASAIL("parasail"),
		PARIGP("parigp"),
		PASCAL("pascal"),
		PAWN("pawn"),
		PCRE("pcre"),
		PER("per"),
		PERL("perl"),
		PERL6("perl6"),
		PHP("php"),
		PHPBRIEF("php-brief"),
		PIC16("pic16"),
		PIKE("pike"),
		PIXELBENDER("pixelbender"),
		PLI("pli"),
		PLSQL("plsql"),
		POSTGRESQL("postgresql"),
		POSTSCRIPT("postscript"),
		POVRAY("povray"),
		POWERSHELL("powershell"),
		POWERBUILDER("powerbuilder"),
		PROFTPD("proftpd"),
		PROGRESS("progress"),
		PROLOG("prolog"),
		PROPERTIES("properties"),
		PROVIDEX("providex"),
		PUPPET("puppet"),
		PUREBASIC("purebasic"),
		PYCON("pycon"),
		PYTHON("python"),
		PYS60("pys60"),
		Q("q"),
		QBASIC("qbasic"),
		QML("qml"),
		RSPLUS("rsplus"),
		RACKET("racket"),
		RAILS("rails"),
		RBS("rbs"),
		REBOL("rebol"),
		REG("reg"),
		REXX("rexx"),
		ROBOTS("robots"),
		RPMSPEC("rpmspec"),
		RUBY("ruby"),
		GNUPLOT("gnuplot"),
		RUST("rust"),
		SAS("sas"),
		SCALA("scala"),
		SCHEME("scheme"),
		SCILAB("scilab"),
		SCL("scl"),
		SDLBASIC("sdlbasic"),
		SMALLTALK("smalltalk"),
		SMARTY("smarty"),
		SPARK("spark"),
		SPARQL("sparql"),
		SQF("sqf"),
		SQL("sql"),
		STANDARDML("standardml"),
		STONESCRIPT("stonescript"),
		SCLANG("sclang"),
		SWIFT("swift"),
		SYSTEMVERILOG("systemverilog"),
		TSQL("tsql"),
		TCL("tcl"),
		TERATERM("teraterm"),
		THINBASIC("thinbasic"),
		TYPOSCRIPT("typoscript"),
		UNICON("unicon"),
		USCRIPT("uscript"),
		UPC("upc"),
		URBI("urbi"),
		VALA("vala"),
		VBNET("vbnet"),
		VBSCRIPT("vbscript"),
		VEDIT("vedit"),
		VERILOG("verilog"),
		VHDL("vhdl"),
		VIM("vim"),
		VISUALPROLOG("visualprolog"),
		VB("vb"),
		VISUALFOXPRO("visualfoxpro"),
		WHITESPACE("whitespace"),
		WHOIS("whois"),
		WINBATCH("winbatch"),
		XBASIC("xbasic"),
		XML("xml"),
		XORG_CONF("xorg_conf"),
		XPP("xpp"),
		YAML("yaml"),
		Z80("z80"),
		ZXBASIC("zxbasic");

		private String apiName;
		
		private Language(String apiName) {
			this.apiName = apiName;
		}
		
		public String getAPIName(){
			return apiName;
		}
	}
	
	/**
	 * Paste Visibility
	 * 
	 * @author rodel77
	 */
	public enum Visibility{
		PUBLIC(0),
		UNLISTED(1),
		PRIVATE(2);
		
		private int id;
		
		private Visibility(int id) {
			this.id = id;
		}
		
		public int getID(){
			return id;
		}
	}
	
	/**
	 * Paste expire
	 * 
	 * @author rodel77
	 */
	public enum Expire{
		NEVER("N"),
		TEN_MINUTES("10M"),
		ONE_HOUR("1H"),
		ONE_DAY("1D"),
		ONE_WEEK("1W"),
		TWO_WEEKS("2W"),
		ONE_MONTH("1M");
		
		private String apiName;
		
		private Expire(String apiName) {
			this.apiName = apiName;
		}
		
		public String getAPIName(){
			return apiName;
		}
	}

	/**
	 * Paste Result
	 * 
	 * @author rodel77
	 */
	public class PasteResult{
		private String errorMessage = "";
		private String pasteURL = "";
		private boolean valid = false;
		
		/**
		 * Create PasteResult (Only for internal API)
		 * 
		 * @param errorMessage
		 * @param pasteURL
		 * @param valid
		 */
		public PasteResult(String errorMessage, String pasteURL, boolean valid) {
			this.errorMessage = errorMessage;
			this.pasteURL = pasteURL;
			this.valid = valid;
		}
		
		/**
		 * Get error message
		 * <br>
		 * <b>Note: First check {@link PasteResult#isValid()}</b>
		 * @return
		 */
		public String getErrorMessage() {
			return errorMessage;
		}
		
		/**
		 * Get paste URL
		 * <br>
		 * <b>Note: First check {@link PasteResult#isValid()}</b>
		 * @return
		 */
		public String getPasteURL(){
			return pasteURL;
		}
		
		/**
		 * Check if valid
		 * 
		 * @return
		 */
		public boolean isValid() {
			return valid;
		}
		
		/**
		 * If valid return paste url otherwise error message
		 * 
		 * @return
		 */
		@Override
		public String toString() {
			if(valid) {
				return pasteURL;
			}else{
				return errorMessage;
			}
		}
	}
	
	@Override
	public String toString() {
		Map<String, String> vals = new HashMap<>();
		vals.put("code", code);
		vals.put("name", fileName);
		vals.put("expire", expire.name());
		vals.put("visibility", visibility.name());
		vals.put("language", language.name());
		return "Pastebin"+vals.toString();
	}
}