import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.text.JTextComponent;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.function.FailableBiConsumer;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.validator.routines.UrlValidator;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlTextArea;
import org.javatuples.Unit;
import org.javatuples.valueintf.IValue0;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.meeuw.functional.ThrowingRunnable;
import org.meeuw.functional.TriPredicate;
import org.oxbow.swingbits.util.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;
import net.miginfocom.swing.MigLayout;

public class OpenJTalkPanel extends JPanel
		implements InitializingBean, ApplicationContextAware, ActionListener, ListDataListener {

	private static final long serialVersionUID = 1679789881293611910L;

	private static final Logger LOG = LoggerFactory.getLogger(OpenJTalkPanel.class);

	private static final String VALUE = "value";

	private transient ApplicationContext applicationContext = null;

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Name {
		String value();
	}

	@Name("SYNTEXT")
	private JTextComponent taText = null;

	@Name("SYNALPHA")
	private JTextComponent tfQuality = null;

	@Name("F0SHIFT")
	private JTextComponent tfPitch = null;

	@Name("DURATION")
	private JTextComponent tfDuration = null;

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	@Note("URL")
	private JTextComponent tfUrl = null;

	@Note("Error Message")
	private JTextComponent tfErrorMessage = null;

	private JTextComponent tfElapsed = null;

	@Name("SPKR")
	private transient ComboBoxModel<?> cbmVoice = null;

	@Note("Copy")
	private AbstractButton btnCopy = null;

	@Note("Download")
	private AbstractButton btnDownload = null;

	private AbstractButton btnPlayAudio = null;

	private Map<String, String> voices = null;

	private String key = null;

	private ObjectMapper objectMapper = null;

	private transient Entry<URL, File> entry = null;

	private transient Iterable<DefaultListModel<?>> dlms = null;

	private DefaultListModel<Object> listModel = null;

	private IValue0<Map<String, String>> iValue0Map = null;

	private String url = null;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//
		setLayout(this, new MigLayout());
		//
		final Collection<String> urls = Collections.singleton(url = "https://open-jtalk.sp.nitech.ac.jp/");
		//
		final org.jsoup.nodes.Document document = testAndApply(Objects::nonNull,
				testAndApply(Objects::nonNull,
						testAndApply(x -> IterableUtils.size(x) == 1, urls, x -> IterableUtils.get(x, 0), null),
						URL::new, null),
				x -> Jsoup.parse(x, 0), null);
		//
		// 合成テキスト(最大200字)
		//
		Iterable<Element> elements = select(document, "textarea");
		//
		testAndRunThrows(IterableUtils.size(elements) > 1, () -> {
			//
			throw new IllegalStateException();
			//
		});
		//
		Element element = testAndApply(x -> IterableUtils.size(x) == 1, elements, x -> IterableUtils.get(x, 0), null);
		//
		final UnaryOperator<Element> parentPreviousElementSibling = x -> previousElementSibling(parent(x));
		//
		add(new JLabel(StringUtils
				.defaultIfBlank(collect(map(stream(childNodes(apply(parentPreviousElementSibling, element))), x -> {
					//
					if (x instanceof TextNode textNode) {
						//
						return text(textNode);
						//
					} else if (x instanceof Element e && equals(Strings.CI, tagName(e), "br")) {
						//
						return "<br/>";
						//
					} // if
						//
					return toString(x);
					//
				}), Collectors.joining("", "<html>", "</html>")), "Text")));
		//
		final int width = 375;
		//
		final String wrap = "wrap";
		//
		final PropertyResolver propertyResolver = getEnvironment(applicationContext);
		//
		add(new JScrollPane(
				taText = new JTextArea(toString(testAndApply(OpenJTalkPanel::containsProperty, propertyResolver,
						String.join(".", getName(getClass(this)), "SYNTEXT"), OpenJTalkPanel::getProperty, null)))),
				String.format("%1$s,growy,wmin %2$spx", wrap, width));
		//
		// 話者
		//
		testAndRunThrows(IterableUtils.size(elements = select(document, "select")) > 1, () -> {
			//
			throw new IllegalStateException();
			//
		});
		//
		add(new JLabel(StringUtils.defaultIfBlank(text(apply(parentPreviousElementSibling,
				element = testAndApply(x -> IterableUtils.size(x) == 1, elements, x -> IterableUtils.get(x, 0), null))),
				"Voice")));
		//
		add(cast(Component.class,
				testAndApply(Objects::nonNull,
						cbmVoice = testAndApply(Objects::nonNull,
								toArray(values(voices = collect(stream(children(element)),
										Collectors.toMap(x -> attr(x, VALUE), OpenJTalkPanel::text))), new String[] {}),
								DefaultComboBoxModel::new, null),
						JComboBox::new, x -> new JComboBox<>())));
		//
		testAndAccept(Objects::nonNull,
				getVoice(propertyResolver, String.join(".", getName(getClass(this)), "SPKR"), cbmVoice, voices),
				x -> setSelectedItem(cbmVoice, getValue0(x)));
		//
		add(new JLabel("最小"));
		//
		add(new JLabel("標準"));
		//
		add(new JLabel("最大"), wrap);
		//
		// 声質
		//
		String label = "声質";
		//
		testAndRunThrows(IterableUtils.size(elements = getParentPreviousElementSiblingByLabel(document, label)) > 1,
				() -> {
					//
					throw new IllegalStateException();
					//
				});
		//
		add(new JLabel(label));
		//
		final Pattern pattern = Pattern.compile("^\\((-?\\d+(\\.\\d+)?)〜(\\d+(\\.\\d+)?), 標準: (\\d+(\\.\\d+)?)\\)$");
		//
		Triple<String, String, String> triple = getTriple(pattern,
				element = testAndApply(x -> IterableUtils.size(x) == 1, elements, x -> IterableUtils.get(x, 0), null));
		//
		final Function<Triple<?, ?, ?>, String> function = x -> iif(x != null, String.format("wmin %1$spx", width),
				String.format("wmin %1$spx,%2$s", width, wrap));
		//
		add(tfQuality = new JTextField(StringUtils.defaultString(testAndApply((a, b, c) -> containsProperty(a, b),
				propertyResolver, String.join(".", getName(getClass(this)), "SYNALPHA"), element,
				(a, b, c) -> getProperty(a, b), (a, b, c) -> attr(c, VALUE)))), apply(function, triple));
		//
		final FailableConsumer<Triple<String, String, String>, RuntimeException> consumer = x -> {
			//
			add(new JLabel(getLeft(x)));
			//
			add(new JLabel(getMiddle(x)));
			//
			add(new JLabel(getRight(x)), wrap);
			//
		};
		//
		testAndAccept(Objects::nonNull, triple, consumer);
		//
		// ピッチシフト
		//
		testAndRunThrows(
				IterableUtils.size(elements = getParentPreviousElementSiblingByLabel(document, label = "ピッチシフト")) > 1,
				() -> {
					//
					throw new IllegalStateException();
					//
				});
		//
		add(new JLabel(label));
		//
		add(tfPitch = new JTextField(StringUtils.defaultString(testAndApply((a, b, c) -> containsProperty(a, b),
				propertyResolver, String.join(".", getName(getClass(this)), "F0SHIFT"),
				element = testAndApply(x -> IterableUtils.size(x) == 1, elements, x -> IterableUtils.get(x, 0), null),
				(a, b, c) -> getProperty(a, b), (a, b, c) -> attr(c, VALUE)))),
				apply(function, triple = getTriple(pattern, element)));
		//
		testAndAccept(Objects::nonNull, triple, consumer);
		//
		// 話速
		//
		testAndRunThrows(
				IterableUtils.size(elements = getParentPreviousElementSiblingByLabel(document, label = "話速")) > 1,
				() -> {
					//
					throw new IllegalStateException();
					//
				});
		//
		add(new JLabel(label));
		//
		add(tfDuration = new JTextField(
				StringUtils.defaultString(testAndApply((a, b, c) -> OpenJTalkPanel.containsProperty(a, b),
						propertyResolver, String.join(".", getName(getClass(this)), "DURATION"),
						element = testAndApply(x -> IterableUtils.size(x) == 1, elements, x -> IterableUtils.get(x, 0),
								null),
						(a, b, c) -> OpenJTalkPanel.getProperty(a, b), (a, b, c) -> attr(c, VALUE)))),
				apply(function, triple = getTriple(pattern, element)));
		//
		testAndAccept(Objects::nonNull, triple, consumer);
		//
		add(new JLabel());
		//
		final JPanel panel = new JPanel();
		//
		final LayoutManager layoutManager = panel.getLayout();
		//
		if (layoutManager instanceof FlowLayout flowLayout) {
			//
			flowLayout.setVgap(0);
			//
		} // if
			//
		panel.add(btnDownload = new JButton("Download"));
		//
		panel.add(btnPlayAudio = new JButton("Play Audio"));
		//
		add(panel, wrap);
		//
		add(new JLabel("Elpased"));
		//
		add(tfElapsed = new JTextField(), String.format("%1$s,wmin %2$spx", wrap, width));
		//
		add(new JLabel("Output"));
		//
		add(tfUrl = new JTextField(), String.format("wmin %1$spx", width));
		//
		add(btnCopy = new JButton("Copy"), String.format("%1$s,span %2$s", wrap, 2));
		//
		add(new JLabel("Error"));
		//
		add(tfErrorMessage = new JTextField(), String.format("%1$s,wmin %2$spx", wrap, width));
		//
		forEach(Arrays.asList(tfElapsed, tfUrl, tfErrorMessage), x -> setEditable(x, false));
		//
		setEnabled(btnCopy, false);
		//
		forEach(filter(map(filter(stream(FieldUtils.getAllFieldsList(getClass())), f -> !isStatic(f)),
				f -> cast(AbstractButton.class, Narcissus.getField(this, f))), Objects::nonNull),
				x -> addActionListener(x, this));
		//
	}

	private static Environment getEnvironment(final EnvironmentCapable instance) {
		return instance != null ? instance.getEnvironment() : null;
	}

	private static boolean containsProperty(final PropertyResolver instance, final String key) {
		return instance != null && instance.containsProperty(key);
	}

	private static String getProperty(final PropertyResolver instance, final String key) {
		return instance != null ? instance.getProperty(key) : null;
	}

	private static void addActionListener(final AbstractButton instance, final ActionListener actionListener) {
		if (instance != null) {
			instance.addActionListener(actionListener);
		}
	}

	private static boolean isStatic(final Member instance) {
		return instance != null && Modifier.isStatic(instance.getModifiers());
	}

	private static <T> void forEach(final Stream<T> instance, final Consumer<? super T> action) {
		if (instance != null && (Proxy.isProxyClass(getClass(instance)) || action != null)) {
			instance.forEach(action);
		}
	}

	private static void setEditable(final JTextComponent instance, final boolean flag) {
		if (instance != null) {
			instance.setEditable(flag);
		}
	}

	private static <T> void forEach(final Iterable<T> instance, final Consumer<? super T> action) {
		if (instance != null && (action != null || Proxy.isProxyClass(getClass(instance)))) {
			instance.forEach(action);
		}
	}

	private static <L> L getLeft(final Triple<L, ?, ?> instance) {
		return instance != null ? instance.getLeft() : null;
	}

	private static <M> M getMiddle(final Triple<?, M, ?> instance) {
		return instance != null ? instance.getMiddle() : null;
	}

	private static <R> R getRight(final Triple<?, ?, R> instance) {
		return instance != null ? instance.getRight() : null;
	}

	private static void setSelectedItem(final ComboBoxModel<?> instance, final Object selectedItem) {
		if (instance != null) {
			instance.setSelectedItem(selectedItem);
		}
	}

	private static <X> X getValue0(final IValue0<X> instance) {
		return instance != null ? instance.getValue0() : null;
	}

	private static <T> T cast(final Class<T> clz, final Object value) {
		return clz != null && clz.isInstance(value) ? clz.cast(value) : null;
	}

	private static String attr(final Node instance, final String attributeKey) {
		return instance != null && attributeKey != null ? instance.attr(attributeKey) : null;
	}

	private static <T> T[] toArray(final Collection<T> instance, final T[] array) {
		//
		return instance != null && (array != null || Proxy.isProxyClass(getClass(instance))) ? instance.toArray(array)
				: null;
		//
	}

	private static <V> Collection<V> values(final Map<?, V> instance) {
		return instance != null ? instance.values() : null;
	}

	private static Elements children(final Element instance) {
		//
		try {
			//
			return instance != null && FieldUtils.readField(instance, "childNodes", true) != null ? instance.children()
					: null;
			//
		} catch (final IllegalAccessException e) {
			//
			return null;
			//
		} // try
			//
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		//
		return instance != null && (predicate != null || Proxy.isProxyClass(getClass(instance)))
				? instance.filter(predicate)
				: null;
		//
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static String tagName(final Element instance) {
		//
		try {
			//
			return instance != null && FieldUtils.readField(instance, "tag", true) != null ? instance.tagName() : null;
			//
		} catch (IllegalAccessException e) {
			//
			return null;
			//
		} // try
			//
	}

	private static String text(final Element instance) {
		//
		try {
			//
			return instance != null && FieldUtils.readField(instance, "childNodes", true) != null ? instance.text()
					: null;
			//
		} catch (final IllegalAccessException e) {
			//
			return null;
			//
		} // try
			//
	}

	private static String text(final TextNode instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		try {
			//
			if (FieldUtils.readField(instance, "value", true) == null) {
				//
				return null;
				//
			} // if
				//
		} catch (final IllegalAccessException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
		return instance.text();
		//
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector) {
		//
		return instance != null && (collector != null || Proxy.isProxyClass(getClass(instance)))
				? instance.collect(collector)
				: null;
		//
	}

	private static <T, R> Stream<R> map(final Stream<T> instance, final Function<? super T, ? extends R> mapper) {
		//
		return instance != null && (Proxy.isProxyClass(getClass(instance)) || mapper != null) ? instance.map(mapper)
				: null;
		//
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <E> Stream<E> stream(final Collection<E> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static List<Node> childNodes(final Node instance) {
		//
		final List<Field> fs = toList(
				filter(testAndApply(Objects::nonNull, getDeclaredFields(getClass(instance)), Arrays::stream, null),
						f -> Objects.equals(getName(f), "childNodes")));
		//
		final Field f = fs != null && fs.size() == 1 ? fs.get(0) : null;
		//
		if (f != null && Narcissus.getField(instance, f) == null) {
			//
			return null;
			//
		} // if
			//
		return instance != null ? instance.childNodes() : null;
		//
	}

	private static <T> List<T> toList(final Stream<T> instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		try {
			//
			// java.util.stream.AbstractPipeline.sourceStage
			//
			if (isAssignableFrom(Class.forName("java.util.stream.AbstractPipeline"), getClass(instance))) {
				//
				final Stream<Field> s = filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
						f -> Objects.equals(getName(f), "sourceStage"));
				//
				final List<Field> fs = s != null ? s.toList() : null;
				//
				final int size = IterableUtils.size(fs);
				//
				if (size > 1) {
					//
					throw new IllegalStateException();
					//
				} else if (testAndApply(x -> !isStatic(x),
						testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null),
						x -> Narcissus.getField(instance, x), null) == null) {
					//
					return null;
					//
				} // if
					//
			} // if
				//
		} catch (final ClassNotFoundException e) {
			//
			error(LOG, e.getMessage(), e);
			//
		} // try
			//
		return instance.toList();
		//
	}

	private static void error(final Logger instance, final String msg, final Throwable t) {
		//
		if (instance != null) {
			//
			instance.error(msg, t);
			//
		} // if
			//
	}

	private static boolean isAssignableFrom(final Class<?> a, final Class<?> b) {
		return a != null && b != null && a.isAssignableFrom(b);
	}

	private static Field[] getDeclaredFields(final Class<?> instance) {
		return instance != null ? instance.getDeclaredFields() : null;
	}

	private static <T, R> R apply(final Function<T, R> instance, final T value) {
		return instance != null ? instance.apply(value) : null;
	}

	private static <T, U, R> R apply(final BiFunction<T, U, R> instance, final T t, final U u) {
		return instance != null ? instance.apply(t, u) : null;
	}

	private static final Element parent(final Element instnace) {
		return instnace != null ? instnace.parent() : null;
	}

	private static Element previousElementSibling(final Element instance) {
		return instance != null ? instance.previousElementSibling() : null;
	}

	private static Elements select(final Element instance, final String cssQuery) {
		//
		try {
			//
			return instance != null && FieldUtils.readField(instance, "childNodes", true) != null
					? instance.select(cssQuery)
					: null;
			//
		} catch (final IllegalAccessException e) {
			//
			return null;
			//
		} // try
			//
	}

	private static void setEnabled(final AbstractButton instance, final boolean b) {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		final Collection<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
				f -> Objects.equals(getName(f), "model")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		if (f == null || Narcissus.getField(instance, f) != null) {
			//
			instance.setEnabled(b);
			//
		} // if
			//
	}

	private static IValue0<Object> getVoice(final PropertyResolver propertyResolver, final String propertyKey,
			final ListModel<?> listModel, final Map<?, ?> map) {
		//
		IValue0<Object> iValue0 = null;
		//
		if (containsProperty(propertyResolver, propertyKey)) {
			//
			final String propertyValue = getProperty(propertyResolver, propertyKey);
			//
			Object elementAt = null;
			//
			for (int i = 0; i < getSize(listModel); i++) {
				//
				if (!Objects.equals(toString(elementAt = getElementAt(listModel, i)), get(map, propertyValue))) {
					//
					continue;
					//
				} // if
					//
				testAndRunThrows(iValue0 != null, () -> {
					//
					throw new IllegalStateException();
					//
				});
				//
				iValue0 = Unit.with(elementAt);
				//
			} // for
				//
			if (iValue0 == null) {
				//
				for (int i = 0; i < getSize(listModel); i++) {
					//
					if (!contains(Strings.CI, toString(elementAt = getElementAt(listModel, i)), propertyValue)) {
						//
						continue;
						//
					} // if
						//
					testAndRunThrows(iValue0 != null, () -> {
						//
						throw new IllegalStateException();
						//
					});
					//
					iValue0 = Unit.with(elementAt);
					//
				} // for
					//
			} // if
				//
		} // if
			//
		return iValue0;
		//
	}

	private static boolean contains(final Strings instance, final CharSequence str, final CharSequence searchStr) {
		return instance != null && instance.contains(str, searchStr);
	}

	private static String toString(final Object instance) {
		return instance != null ? instance.toString() : null;
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static int getSize(final ListModel<?> instance) {
		return instance != null ? instance.getSize() : 0;
	}

	private static <E> E getElementAt(final ListModel<E> instance, final int index) {
		return instance != null ? instance.getElementAt(index) : null;
	}

	private static <T> T iif(final boolean condition, final T valueTrue, final T valueFalse) {
		return condition ? valueTrue : valueFalse;
	}

	private static Triple<String, String, String> getTriple(final Pattern pattern, final Element element) {
		//
		final Matcher matcher = matcher(pattern, toString(nextSibling(element)));
		//
		return matches(matcher) && groupCount(matcher) > 5
				? Triple.of(group(matcher, 1), group(matcher, 5), group(matcher, 3))
				: null;
		//
	}

	private static String group(final Matcher instance, final int group) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final List<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
				f -> Objects.equals(getName(f), "parentPattern")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		if (f != null && Narcissus.getField(instance, f) == null) {
			//
			return null;
			//
		} // if
			//
		return instance.group(group);
		//
	}

	private static int groupCount(final Matcher instance) {
		//
		if (instance == null) {
			//
			return 0;
			//
		} // if
			//
		final List<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
				f -> Objects.equals(getName(f), "parentPattern")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		if (f != null && Narcissus.getField(instance, f) == null) {
			//
			return 0;
			//
		} // if
			//
		return instance.groupCount();
		//
	}

	private static Matcher matcher(final Pattern instance, final CharSequence input) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Stream<Field> fs = testAndApply(Objects::nonNull, getDeclaredFields(Pattern.class), Arrays::stream, null);
		//
		if (testAndApply(Objects::nonNull, testAndApply(x -> IterableUtils.size(x) == 1,
				toList(filter(fs, x -> Objects.equals(getName(x), "pattern"))), x -> IterableUtils.get(x, 0), null),
				x -> Narcissus.getObjectField(instance, x), null) == null) {
			//
			return null;
			//
		} // if
			//
		return input != null ? instance.matcher(input) : null;
		//
	}

	private static boolean matches(final Matcher instance) {
		//
		if (instance == null) {
			//
			return false;
			//
		} // if
			//
		final Stream<Field> fs = testAndApply(Objects::nonNull, getDeclaredFields(Matcher.class), Arrays::stream, null);
		//
		if (testAndApply(Objects::nonNull, testAndApply(x -> IterableUtils.size(x) == 1,
				toList(filter(fs, x -> Objects.equals(getName(x), "groups"))), x -> IterableUtils.get(x, 0), null),
				x -> Narcissus.getObjectField(instance, x), null) == null) {
			//
			return false;
			//
		} // if
			//
		return instance.matches();
		//
	}

	private static Node nextSibling(final Node instance) {
		return instance != null ? instance.nextSibling() : null;
	}

	private static Iterable<Element> getParentPreviousElementSiblingByLabel(final Element element, final String label) {
		//
		return toList(filter(selectStream(element, "input[type=\"text\"]"),
				x -> equals(Strings.CS, text(previousElementSibling(parent(x))), label)));
		//
	}

	private static boolean equals(final Strings instance, final CharSequence cs1, final CharSequence cs2) {
		return instance != null && instance.equals(cs1, cs2);
	}

	private static Stream<Element> selectStream(final Element instance, final String cssQuery) {
		return instance != null && StringUtils.isNotBlank(cssQuery) ? instance.selectStream(cssQuery) : null;
	}

	private static <E extends Throwable> void testAndRunThrows(final boolean b,
			final ThrowingRunnable<E> throwingRunnable) throws E {
		if (b) {
			runThrows(throwingRunnable);
		}
	}

	private static <T extends Throwable> void runThrows(final ThrowingRunnable<T> instance) throws T {
		if (instance != null) {
			instance.runThrows();
		}
	}

	private static void setLayout(final Container instance, final LayoutManager layoutManager) {
		if (instance != null) {
			instance.setLayout(layoutManager);
		}
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static final <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, U, R> R testAndApply(final BiPredicate<T, U> predicate, final T t, final U u,
			final BiFunction<T, U, R> functionTrue, final BiFunction<T, U, R> functionFalse) {
		return test(predicate, t, u) ? apply(functionTrue, t, u) : apply(functionFalse, t, u);
	}

	private static <T, U> boolean test(final BiPredicate<T, U> instance, final T t, final U u) {
		return instance != null && instance.test(t, u);
	}

	private static <T, U, V, R> R testAndApply(final TriPredicate<T, U, V> predicate, final T t, final U u, final V v,
			final TriFunction<T, U, V, R> functionTrue, final TriFunction<T, U, V, R> functionFalse) {
		return test(predicate, t, u, v) ? apply(functionTrue, t, u, v) : apply(functionFalse, t, u, v);
	}

	private static <T, U, V, R> R apply(final TriFunction<T, U, V, R> instnace, final T t, final U u, final V v) {
		return instnace != null ? instnace.apply(t, u, v) : null;
	}

	private static <T, U, V> boolean test(final TriPredicate<T, U, V> instance, final T t, final U u, final V v) {
		return instance != null && instance.test(t, u, v);
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		final Object source = getSource(evt);
		//
		if (Objects.equals(source, btnDownload)) {
			//
			final Stopwatch stopwatch = Stopwatch.createStarted();
			//
			URL u = null;
			//
			final String keyTemp = sha512Hex(this,
					objectMapper = ObjectUtils.getIfNull(objectMapper, ObjectMapper::new));
			//
			testAndRunThrows(!Objects.equals(keyTemp, key), () -> setText(tfUrl, (String) null));
			//
			setText(tfErrorMessage, (String) null);
			//
			try {
				//
				if ((u = testAndApply(StringUtils::isNotBlank, getText(tfUrl), URL::new, null)) == null) {
					//
					final List<String> keys = toList(map(
							filter(stream(entrySet(voices)),
									x -> Objects.equals(getValue(x), getSelectedItem(cbmVoice))),
							OpenJTalkPanel::getKey));
					//
					testAndRunThrows(IterableUtils.size(keys) > 1, () -> {
						//
						throw new IllegalStateException();
						//
					});
					//
					final Map<String, Object> map = filter(
							map(filter(Stream.of(tfQuality, tfPitch), Objects::nonNull),
									x -> getStringObjectEntry(getAnnotatedElementObjectEntry(this, x))),
							Objects::nonNull).collect(LinkedHashMap::new, (k, v) -> put(k, getKey(v), getValue(v)),
									OpenJTalkPanel::putAll);
					//
					setText(tfUrl,
							toString(u = execute(
									getText(taText), testAndApply(y -> IterableUtils.size(y) == 1, keys,
											y -> IterableUtils.get(y, 0), null),
									NumberUtils.toInt(getText(tfDuration), 0)// TODO
									, map)));
					//
					key = keyTemp;
					//
				} // if
					//
			} catch (final RuntimeException | MalformedURLException e) {
				//
				setText(tfUrl, (String) null);
				//
				setText(tfErrorMessage, e.getMessage());
				//
				return;
				//
			} finally {
				//
				setText(tfElapsed, stopwatch);
				//
				setEnabled(btnCopy, isValid(UrlValidator.getInstance(), getText(tfUrl)));
				//
			} // try
				//
			final JFileChooser jfc = new JFileChooser(".");
			//
			setFileName(cast(BasicFileChooserUI.class, jfc.getUI()), StringUtils.substringAfterLast(getFile(u), '/'));
			//
			if (and(jfc, x -> Boolean.logicalAnd(!isTestMode(), !GraphicsEnvironment.isHeadless()),
					x -> equals(showSaveDialog(x, null), JFileChooser.APPROVE_OPTION))) {
				//
				try (final InputStream is = openStream(u)) {
					//
					testAndAccept(Objects::nonNull, testAndApply(Objects::nonNull, is, IOUtils::toByteArray, null),
							x -> FileUtils.writeByteArrayToFile(jfc.getSelectedFile(), x));
					//
				} catch (final IOException e) {
					//
					error(LOG, e.getMessage(), e);
					//
				} // try
					//
			} // if
				//
			setText(tfElapsed, stopwatch);
			//
		} else
			try {
				//
				if (actionPerformed(source)) {
					//
					return;
					//
				} // if
					//
			} catch (final IOException | UnsupportedAudioFileException | LineUnavailableException e) {
				//
				setText(tfUrl, (String) null);
				//
				setText(tfErrorMessage, e.getMessage());
				//
				return;
				//
			} // if
				//
	}

	private static boolean isValid(final UrlValidator instance, final String value) {
		return instance != null && instance.isValid(value);
	}

	private static InputStream openStream(final URL instance) throws IOException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Collection<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
				f -> Objects.equals(getName(f), "handler")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		return f != null && Narcissus.getField(instance, f) == null ? null : instance.openStream();
		//
	}

	private static String getFile(final URL instance) {
		return instance != null ? instance.getFile() : null;
	}

	private static <K, V> void putAll(final Map<K, V> a, final Map<? extends K, ? extends V> b) {
		if (a != null && (b != null || Proxy.isProxyClass(getClass(a)))) {
			a.putAll(b);
		}
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static String getText(final JTextComponent instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		try {
			//
			if (Narcissus.getField(instance, Narcissus.findField(getClass(instance), "model")) == null) {
				//
				return null;
				//
			} // if
				//
		} catch (final NoSuchFieldException e) {
			//
			error(LOG, e.getMessage(), e);
			//
		} // try
			//
		return instance.getText();
		//
	}

	private static void setText(final JTextComponent instance, final String text) {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		try {
			//
			if (Narcissus.getField(instance, Narcissus.findField(getClass(instance), "model")) == null) {
				//
				return;
				//
			} // if
				//
		} catch (final NoSuchFieldException e) {
			//
			error(LOG, e.getMessage(), e);
			//
		} // try
			//
		instance.setText(text);
		//
	}

	private static String sha512Hex(final OpenJTalkPanel instance, final ObjectMapper objectMapper) {
		//
		try {
			//
			return testAndApply(Objects::nonNull, writeValueAsString(objectMapper,
					instance != null ? new Object[] { getText(instance.taText), getSelectedItem(instance.cbmVoice),
							getText(instance.tfQuality), getText(instance.tfPitch), getText(instance.tfDuration) }
							: null),
					DigestUtils::sha512Hex, null);
			//
		} catch (final JsonProcessingException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
	}

	private static String writeValueAsString(final ObjectMapper instance, final Object value)
			throws JsonProcessingException {
		return instance != null ? instance.writeValueAsString(value) : null;
	}

	private static Object getSelectedItem(final ComboBoxModel<?> instance) {
		return instance != null ? instance.getSelectedItem() : null;
	}

	private static Object getSource(final EventObject instance) {
		return instance != null ? instance.getSource() : null;
	}

	private static void setText(final JTextComponent jtc, final Stopwatch stopwatch) {
		//
		final String string = toString(elapsed(stopwatch));
		//
		final Matcher matcher = matcher(Pattern.compile("^PT(((\\d+)(.\\d+)?)S)$"), string);
		//
		testAndRun(and(matcher, OpenJTalkPanel::matches, x -> groupCount(x) > 0), () -> setText(jtc, group(matcher, 1)),
				() -> setText(jtc, string));
		//
	}

	private static Duration elapsed(final Stopwatch instance) {
		return instance != null ? instance.elapsed() : null;
	}

	private boolean actionPerformed(final Object source)
			throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		//
		if (Objects.equals(source, btnCopy)) {
			//
			testAndRunThrows(!isTestMode(),
					() -> setContents(getSystemClipboard(getToolkit()), new StringSelection(getText(tfUrl)), null));
			//
			return true;
			//
		} else if (Objects.equals(source, btnPlayAudio)) {
			//
			final Stopwatch stopwatch = Stopwatch.createStarted();
			//
			final String keyTemp = sha512Hex(this,
					objectMapper = ObjectUtils.getIfNull(objectMapper, ObjectMapper::new));
			//
			if (!Objects.equals(keyTemp, key)) {
				//
				setText(tfUrl, (String) null);
				//
				entry = null;
				//
			} // if
				//
			setText(tfErrorMessage, (String) null);
			//
			final File file = getValue(entry);
			//
			if (and(exists(file), isFile(file), canRead(file))) {
				//
				speak(createInputStreamSource(file));
				//
				setText(tfElapsed, stopwatch);
				//
				return true;
				//
			} // if
				//
			final String urlString = getText(tfUrl);
			//
			URL u = null;
			//
			try {
				//
				u = testAndApply(x -> isValid(UrlValidator.getInstance(), x), urlString, URL::new, null);
				//
			} catch (final MalformedURLException e) {
				//
				throw new RuntimeException(e);
				//
			} finally {
				//
				setText(tfElapsed, stopwatch);
				//
			} // try
				//
			final Iterable<Method> ms = collect(
					filter(testAndApply(Objects::nonNull, getDeclaredMethods(getClass(this)), Arrays::stream, null),
							m -> Boolean.logicalAnd(Objects.equals(getName(m), "speak"),
									Arrays.equals(getParameterTypes(m),
											new Class<?>[] { URL.class, DefaultListModel.class }))),
					Collectors.toList());
			//
			testAndRunThrows(IterableUtils.size(ms) > 1, () -> {
				//
				throw new IllegalStateException();
				//
			});
			//
			final Method m = testAndApply(x -> IterableUtils.size(x) == 1, ms, x -> IterableUtils.get(x, 0), null);
			//
			if (Boolean.logicalAnd(isStatic(m), u != null)) {
				//
				Narcissus.invokeStaticMethod(m, u,
						testAndApply(x -> IterableUtils.size(x) == 1, dlms, x -> IterableUtils.get(x, 0), null));
				//
				setText(tfElapsed, stopwatch);
				//
				return true;
				//
			} // if
				//
			final List<String> keys = toList(
					map(filter(stream(entrySet(voices)), x -> Objects.equals(getValue(x), getSelectedItem(cbmVoice))),
							OpenJTalkPanel::getKey));
			//
			testAndRunThrows(IterableUtils.size(keys) > 1, () -> {
				//
				throw new IllegalStateException();
				//
			});
			//
			try {
				//
				final Map<String, Object> map = filter(map(filter(Stream.of(tfQuality, tfPitch), Objects::nonNull),
						x -> getStringObjectEntry(getAnnotatedElementObjectEntry(this, x))), Objects::nonNull)
						.collect(LinkedHashMap::new, (k, v) -> put(k, getKey(v), getValue(v)), OpenJTalkPanel::putAll);
				//
				setText(tfUrl,
						toString(speak(getText(taText),
								testAndApply(x -> IterableUtils.size(x) == 1, keys, x -> IterableUtils.get(x, 0), null),
								NumberUtils.toInt(getText(tfDuration), 0)// TODO
								, 0// TODO volume
								, map)));
				//
				key = keyTemp;
				//
			} catch (final RuntimeException e) {
				//
				setText(tfErrorMessage, e.getMessage());
				//
			} finally {
				//
				setText(tfElapsed, stopwatch);
				//
				setEnabled(btnCopy, isValid(UrlValidator.getInstance(), getText(tfUrl)));
				//
			} // try
				//
			return true;
			//
		} // if
			//
		return false;
		//
	}

	private static Class<?>[] getParameterTypes(final Executable instance) {
		return instance != null ? instance.getParameterTypes() : null;
	}

	private static Method[] getDeclaredMethods(final Class<?> instance) {
		return instance != null ? instance.getDeclaredMethods() : null;
	}

	private static <K, V> Set<Entry<K, V>> entrySet(final Map<K, V> instance) {
		return instance != null ? instance.entrySet() : null;
	}

	private static boolean and(final boolean a, final boolean b, final boolean... bs) {
		//
		boolean result = a && b;
		//
		if (!result) {
			//
			return false;
			//
		} // if
			//
		for (int i = 0; bs != null && i < bs.length; i++) {
			//
			if (!(result &= bs[i])) {
				//
				return false;
				//
			} // if
				//
		} // for
			//
		return result;
		//
	}

	private static boolean exists(final File instance) {
		return instance != null && instance.exists();
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.isFile();
	}

	private static Entry<String, Object> getStringObjectEntry(final Entry<AnnotatedElement, Object> entry) {
		//
		final AnnotatedElement ae = getKey(entry);
		//
		if (isAnnotationPresent(ae, Name.class)) {
			//
			final Object v = getValue(entry);
			//
			return Pair.of(value(getAnnotation(ae, Name.class)), v instanceof JTextComponent jtc ? getText(jtc) : v);
			//
		} // if
			//
		return null;
		//
	}

	private static boolean isAnnotationPresent(final AnnotatedElement instance,
			final Class<? extends Annotation> annotationClass) {
		return instance != null && (annotationClass != null || Proxy.isProxyClass(getClass(instance)))
				&& instance.isAnnotationPresent(annotationClass);
	}

	private static <T extends Annotation> T getAnnotation(final AnnotatedElement instance,
			final Class<T> annotationClass) {
		return instance != null && (annotationClass != null || Proxy.isProxyClass(getClass(instance)))
				? instance.getAnnotation(annotationClass)
				: null;
	}

	private static Entry<AnnotatedElement, Object> getAnnotatedElementObjectEntry(final Object instance,
			final Object value) {
		//
		final List<Field> fs = toList(
				filter(stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(isStatic(f) ? Narcissus.getStaticField(f) : Narcissus.getField(instance, f),
								value)));
		//
		testAndRunThrows(IterableUtils.size(fs) > 1, () -> {
			//
			throw new IllegalStateException();
			//
		});
		//
		return IterableUtils.size(fs) == 1 ? Pair.of(IterableUtils.get(fs, 0), value) : null;
		//
	}

	private static boolean canRead(final File instance) {
		return instance != null && instance.canRead();
	}

	private static InputStreamSource createInputStreamSource(final File file) {
		//
		try {
			//
			return testAndApply(Objects::nonNull, testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)), file,
					x -> Files.readAllBytes(toPath(x)), null), ByteArrayResource::new, null);
			//
		} catch (final IOException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
	}

	private static Path toPath(final File instance) {
		return instance != null ? instance.toPath() : null;
	}

	private URL speak(final String text, final String voiceId, final int rate, final int volume,
			final Map<String, Object> map) {
		//
		final URL u = execute(text, voiceId, rate, map);
		//
		try {
			//
			speak(u, listModel = ObjectUtils.getIfNull(listModel, DefaultListModel::new));
			//
		} catch (final IOException | UnsupportedAudioFileException | LineUnavailableException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
		return u;
		//
	}

	private static void speak(final URL u, final DefaultListModel<Object> listModel)
			throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		//
		final byte[] bs = testAndApply(Objects::nonNull, openStream(u), IOUtils::toByteArray, null);
		//
		if (listModel != null) {
			//
			listModel.removeAllElements();
			//
			listModel.addElement(Pair.of(u, bs));
			//
		} // if
			//
		speak(() -> testAndApply(Objects::nonNull, bs, ByteArrayInputStream::new, null));
		//
	}

	private static class IH implements InvocationHandler {

		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			if (Objects.equals(getReturnType(method), Void.TYPE)) {
				//
				return null;
				//
			} // if
				//
			throw new Throwable(getName(method));
			//
		}

		private static Class<?> getReturnType(final Method instance) {
			return instance != null ? instance.getReturnType() : null;
		}

	}

	private static void speak(final InputStreamSource iss)
			throws IOException, UnsupportedAudioFileException, LineUnavailableException {
		//
		try (final InputStream is = getInputStream(iss); final AudioInputStream ais = getAudioInputStream(is)) {
			//
			final AudioFormat af = getFormat(ais);
			//
			final Info info = testAndApply(x -> !isTestMode(), af, x -> new DataLine.Info(SourceDataLine.class, x),
					x -> cast(Info.class, Narcissus.allocateInstance(Info.class)));
			//
			final Collection<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(info))),
					f -> Objects.equals(getName(f), "lineClass")));
			//
			testAndRunThrows(IterableUtils.size(fs) > 1, () -> {
				//
				throw new IllegalStateException();
				//
			});
			//
			final DataLine dl = cast(DataLine.class, testAndApply(
					x -> and(testAndApply(y -> IterableUtils.size(y) == 1, fs, y -> IterableUtils.get(y, 0), null),
							Objects::nonNull, y -> Narcissus.getField(info, y) != null),
					info, AudioSystem::getLine, x -> Reflection.newProxy(SourceDataLine.class, new IH())));
			//
			final byte[] buf = new byte[1024];
			//
			final SourceDataLine sdl = cast(SourceDataLine.class, dl);
			//
			open(sdl, af, buf.length);
			//
			start(dl);
			//
			int len;
			//
			while (ais != null && (len = ais.read(buf)) != -1) {
				//
				write(sdl, buf, 0, len);
				//
			} // while
				//
			drain(dl);
			//
			stop(dl);
			//
			close(dl);
			//
		} // try
			//
	}

	private static int write(final SourceDataLine instance, final byte[] b, final int off, final int len) {
		return instance != null ? instance.write(b, off, len) : 0;
	}

	private static void close(final Line instance) {
		if (instance != null) {
			instance.close();
		}
	}

	private static void stop(final DataLine instance) {
		if (instance != null) {
			instance.stop();
		}
	}

	private static void drain(final DataLine instance) {
		if (instance != null) {
			instance.drain();
		}
	}

	private static void start(final DataLine instance) {
		if (instance != null) {
			instance.start();
		}
	}

	private static void open(final SourceDataLine instance, final AudioFormat format, final int bufferSize)
			throws LineUnavailableException {
		if (instance != null) {
			instance.open(format, bufferSize);
		}
	}

	private static AudioFormat getFormat(final AudioInputStream instance) {
		return instance != null ? instance.getFormat() : null;
	}

	private static AudioInputStream getAudioInputStream(final InputStream instance)
			throws UnsupportedAudioFileException, IOException {
		return instance != null ? AudioSystem.getAudioInputStream(instance) : null;
	}

	private static InputStream getInputStream(final InputStreamSource instance) throws IOException {
		return instance != null ? instance.getInputStream() : null;
	}

	private URL execute(final String text, final String voiceId, final int rate, final Map<String, Object> map) {
		//
		return execute(url, text, getVoices(), voiceId, rate, map);
		//
	}

	private static URL execute(final String url, final String text, final Map<String, String> voices,
			final String voiceId, final int rate, final Map<String, Object> map) {
		//
		try (final WebClient webClient = new WebClient()) {
			//
			final HtmlPage htmlPage = testAndApply(Objects::nonNull, url, webClient::getPage, null);
			//
			if (getElementByName(htmlPage, "SPKR") instanceof HtmlSelect htmlSelect) {
				//
				Integer index = null;
				//
				for (int i = 0; i < IterableUtils.size(getOptions(htmlSelect)); i++) {
					//
					if (!Objects.equals(getAttribute(getOption(htmlSelect, i), "value"), voiceId)) {
						//
						continue;
						//
					} // if
						//
					testAndRunThrows(index != null, () -> {
						//
						throw new IllegalStateException();
						//
					});
					//
					index = Integer.valueOf(i);
					//
				} // for
					//
				setSelectedIndex(htmlSelect, index);
				//
			} // if
				//
			if (getElementByName(htmlPage, "SYNTEXT") instanceof HtmlTextArea htmlTextArea) {
				//
				setTextContent(htmlTextArea, text);
				//
			} // if
				//
			if (getElementByName(htmlPage, "DURATION") instanceof HtmlInput htmlInput) {
				//
				htmlInput.setValue(Integer.toString(rate));
				//
			} // if
				//
			String key = "SYNALPHA";
			//
			if (containsKey(map, key) && getElementByName(htmlPage, key) instanceof HtmlInput htmlInput) {
				//
				htmlInput.setValue(toString(get(map, key)));
				//
			} // if
				//
			if (containsKey(map, key = "F0SHIFT") && getElementByName(htmlPage, key) instanceof HtmlInput htmlInput) {
				//
				htmlInput.setValue(toString(get(map, key)));
				//
			} // if
				//
			final HtmlPage hm = cast(HtmlPage.class,
					click(cast(DomElement.class, querySelector(htmlPage, "input[type=\"submit\"]"))));
			//
			final IValue0<String> attribute = getAttribute(getElementsByTagName(hm, "source"), "src",
					x -> startsWith(Strings.CS, x, "./temp/"));
			//
			if (attribute != null) {
				//
				return new URL(String.join("/", StringUtils.substringBeforeLast(url, "/"),
						StringUtils.substringAfter(getValue0(attribute), '/')));
				//
			} else if (hm != null) {
				//
				final Iterable<DomNode> domNodes = toList(
						filter(stream(hm.querySelectorAll("b")), x -> Objects.equals(getTextContent(x), "合成結果")));
				//
				testAndRunThrows(IterableUtils.size(domNodes) > 1, () -> {
					//
					throw new IllegalStateException();
					//
				});
				//
				throw new RuntimeException(StringUtils.trim(toString(testAndApply(x -> getLength(x) == 1,
						getChildNodes(getNextElementSibling(testAndApply(x -> IterableUtils.size(x) == 1, domNodes,
								x -> IterableUtils.get(x, 0), null))),
						x -> item(x, 0), null))));
				//
			} // if
				//
		} catch (final IOException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
		return null;
		//
	}

	private static void setSelectedIndex(final HtmlSelect instance, final Integer index) {
		//
		if (instance == null || index == null) {
			//
			return;
			//
		} // if
			//
		final Iterable<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(HtmlSelect.class)),
				f -> Objects.equals(getName(f), "attributes_")));
		//
		testAndRunThrows(IterableUtils.size(fs) > 1, () -> {
			//
			throw new IllegalArgumentException();
			//
		});
		//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		if (f == null || Narcissus.getField(instance, f) != null) {
			//
			instance.setSelectedIndex(index.intValue());
			//
		} // if
			//
	}

	private static boolean startsWith(final Strings instance, final CharSequence str, final CharSequence prefix) {
		return instance != null && instance.startsWith(str, prefix);
	}

	private static NodeList getChildNodes(final org.w3c.dom.Node instance) {
		return instance != null ? instance.getChildNodes() : null;
	}

	private static DomElement getNextElementSibling(final DomNode instance) {
		return instance != null ? instance.getNextElementSibling() : null;
	}

	private static org.w3c.dom.Node item(final NodeList instance, final int index) {
		return instance != null ? instance.item(index) : null;
	}

	private static String getAttribute(final org.w3c.dom.Element instance, final String name) {
		return instance != null ? instance.getAttribute(name) : null;
	}

	private static IValue0<String> getAttribute(final NodeList nodeList, final String attrbiuteName,
			final Predicate<String> predicate) {
		//
		org.w3c.dom.Node namedItem = null;
		//
		String nodeValue = null;
		//
		IValue0<String> result = null;
		//
		for (int i = 0; i < getLength(nodeList); i++) {
			//
			if ((namedItem = getNamedItem(getAttributes(item(nodeList, i)), attrbiuteName)) == null) {
				//
				continue;
				//
			} // if
				//
			if (test(predicate, nodeValue = namedItem.getNodeValue())) {
				//
				if (result == null) {
					//
					result = Unit.with(nodeValue);
					//
				} else {
					//
					throw new IllegalStateException();
					//
				} // if
					//
			} // if
				//
		} // for
			//
		return result;
		//
	}

	private static org.w3c.dom.Node getNamedItem(final NamedNodeMap instance, final String name) {
		return instance != null ? instance.getNamedItem(name) : null;
	}

	private static NamedNodeMap getAttributes(final org.w3c.dom.Node instance) {
		return instance != null ? instance.getAttributes() : null;
	}

	private static int getLength(final NodeList instance) {
		return instance != null ? instance.getLength() : 0;
	}

	private static NodeList getElementsByTagName(final org.w3c.dom.Document instance, final String tagname) {
		return instance != null ? instance.getElementsByTagName(tagname) : null;
	}

	private static <N extends DomNode> N querySelector(final DomNode instance, final String selectors) {
		return instance != null ? instance.querySelector(selectors) : null;
	}

	private static <P extends Page> P click(final DomElement instance) throws IOException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Iterable<Field> fs = toList(filter(stream(FieldUtils.getAllFieldsList(getClass(instance))),
				f -> Objects.equals(getName(f), "page_")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		if (f != null && Narcissus.getObjectField(instance, f) == null) {
			//
			return null;
			//
		} // if
			//
		return instance.click();
		//
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static String getTextContent(final org.w3c.dom.Node instance) {
		return instance != null ? instance.getTextContent() : null;
	}

	private static void setTextContent(final org.w3c.dom.Node instance, final String textContent) {
		if (instance != null) {
			instance.setTextContent(textContent);
		}
	}

	private static List<HtmlOption> getOptions(final HtmlSelect instance) {
		return instance != null ? instance.getOptions() : null;
	}

	private static HtmlOption getOption(final HtmlSelect instance, final int index) {
		return instance != null && instance.getOptionSize() > index ? instance.getOption(index) : null;
	}

	private static <E extends DomElement> E getElementByName(final HtmlPage instance, final String name) {
		return instance != null ? instance.getElementByName(name) : null;
	}

	private Map<String, String> getVoices() {
		//
		if (iValue0Map == null) {
			//
			org.jsoup.nodes.Document document = null;
			//
			try {
				//
				document = testAndApply(Objects::nonNull, testAndApply(Objects::nonNull, url, URL::new, null),
						x -> Jsoup.parse(x, 0), null);
				//
			} catch (final IOException e) {
				//
				throw new RuntimeException(e);
				//
			} // try
				//
			iValue0Map = Unit.with(collect(
					stream(children(testAndApply(x -> IterableUtils.size(x) == 1, select(document, "select"),
							x -> IterableUtils.get(x, 0), null))),
					Collectors.toMap(x -> attr(x, "value"), OpenJTalkPanel::text)));
			//
		} // if
			//
		return getValue0(iValue0Map);
		//
	}

	private static boolean equals(final Number a, final int b) {
		return a != null && a.intValue() == b;
	}

	private static Integer showSaveDialog(final JFileChooser instance, final Component parent) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Collection<Field> fs = toList(
				filter(stream(FieldUtils.getAllFieldsList(getClass(instance))), f -> Objects.equals(getName(f), "ui")));
		//
		if (IterableUtils.size(fs) > 1) {
			//
			throw new IllegalStateException();
			//
		} // if
			//
		final Field f = testAndApply(x -> IterableUtils.size(x) == 1, fs, x -> IterableUtils.get(x, 0), null);
		//
		return f != null && Narcissus.getField(instance, f) == null ? null
				: Integer.valueOf(instance.showSaveDialog(parent));
		//
	}

	private static void testAndRun(final boolean condition, final Runnable runnableTrue, final Runnable runnableFalse) {
		if (condition) {
			run(runnableTrue);
		} else {
			run(runnableFalse);
		} // if
	}

	private static void run(final Runnable instance) {
		if (instance != null) {
			instance.run();
		}
	}

	private static <T> boolean and(final T value, final Predicate<T> a, final Predicate<T> b) {
		return test(a, value) && test(b, value);
	}

	private static void setFileName(final BasicFileChooserUI instance, final String filename) {
		if (instance != null) {
			instance.setFileName(filename);
		}
	}

	private static void setContents(final Clipboard instance, final Transferable contents, final ClipboardOwner owner) {
		if (instance != null) {
			instance.setContents(contents, owner);
		}
	}

	private static boolean isTestMode() {
		return forName("org.junit.jupiter.api.Test") != null;
	}

	private static Class<?> forName(final String className) {
		try {
			return StringUtils.isNotBlank(className) ? Class.forName(className) : null;
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	private static Clipboard getSystemClipboard(final Toolkit instance) {
		return instance != null ? instance.getSystemClipboard() : null;
	}

	private static String value(final Name instance) {
		return instance != null ? instance.value() : null;
	}

	private static <T, E extends Throwable> void testAndAccept(final Predicate<T> instance, final T value,
			final FailableConsumer<T, E> consumer) throws E {
		if (test(instance, value)) {
			accept(consumer, value);
		} // if
	}

	private static <T, E extends Throwable> void accept(final FailableConsumer<T, E> instance, final T object)
			throws E {
		if (instance != null) {
			instance.accept(object);
		}
	}

	@Override
	public void intervalAdded(final ListDataEvent evt) {
		//
		final Entry<?, ?> en = cast(Entry.class, testAndApply(x -> x != null && x.size() == 1,
				cast(DefaultListModel.class, getSource(evt)), x -> getElementAt(x, 0), null));
		//
		FileUtils.deleteQuietly(getValue(entry));
		//
		final URL u = cast(URL.class, getKey(en));
		//
		setText(tfUrl, toString(u));
		//
		final byte[] bs = cast(byte[].class, getValue(en));
		//
		File file = null;
		//
		try {
			//
			if ((file = u != null
					? toFile(Path.of(String.join(".", "temp", StringUtils.substringAfterLast(toString(u), "."))))
					: File.createTempFile(nextAlphabetic(RandomStringUtils.secureStrong(), 3), null)) != null) {
				//
				deleteOnExit(file);
				//
			} // if
				//
			testAndAccept((a, b) -> Boolean.logicalAnd(a != null, b != null), file, bs,
					FileUtils::writeByteArrayToFile);
			//
		} catch (final IOException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
		entry = Pair.of(u, file);
		//
	}

	private static void deleteOnExit(final File instance) {
		//
		if (instance == null || Boolean.logicalAnd(
				contains(Arrays.asList(OperatingSystem.WINDOWS, OperatingSystem.LINUX), getOperatingSystem()),
				instance.getPath() == null)) {
			//
			return;
			//
		} // if
			//
		instance.deleteOnExit();
		//
	}

	private static boolean contains(final Collection<?> items, final Object item) {
		return items != null && items.contains(item);
	}

	private static OperatingSystem getOperatingSystem() {
		//
		final String name = getName(getClass(FileSystems.getDefault()));
		//
		final OperatingSystem[] oss = OperatingSystem.values();
		//
		OperatingSystem os = null;
		//
		IValue0<OperatingSystem> iValue0 = null;
		//
		for (int i = 0; i < length(oss); i++) {
			//
			if (!contains(org.apache.commons.lang3.Strings.CI, name, name(os = ArrayUtils.get(oss, i)))) {
				//
				continue;
				//
			} // if
				//
			if (iValue0 == null) {
				//
				iValue0 = Unit.with(os);
				//
			} else {
				//
				throw new IllegalStateException();
				//
			} // if
				//
		} // for
			//
		return getValue0(iValue0);
		//
	}

	private static String name(final Enum<?> instance) {
		return instance != null ? instance.name() : null;
	}

	private static int length(final Object[] instance) {
		return instance != null ? instance.length : 0;
	}

	private static File toFile(final Path instance) {
		return instance != null ? instance.toFile() : null;
	}

	private static <T, U, E extends Throwable> void testAndAccept(final BiPredicate<T, U> instance, final T t,
			final U u, final FailableBiConsumer<T, U, E> consumer) throws E {
		if (test(instance, t, u)) {
			accept(consumer, t, u);
		} // if
	}

	private static <T, U, E extends Throwable> void accept(final FailableBiConsumer<T, U, E> instance, final T t,
			final U u) throws E {
		if (instance != null) {
			instance.accept(t, u);
		}
	}

	private static String nextAlphabetic(final RandomStringUtils instance, final int count) {
		return instance != null ? instance.nextAlphabetic(count) : null;
	}

	@Override
	public void intervalRemoved(final ListDataEvent evt) {
		//
	}

	@Override
	public void contentsChanged(final ListDataEvent evt) {
		//
	}

	public static void main(final String[] args) throws Exception {
		//
		final JFrame jFrame = new JFrame();
		//
		jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		//
		final OpenJTalkPanel openJTalkPanel = new OpenJTalkPanel();
		//
		openJTalkPanel.afterPropertiesSet();
		//
		jFrame.add(openJTalkPanel);
		//
		jFrame.pack();
		//
		jFrame.setVisible(true);
		//
	}

}