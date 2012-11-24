package net.simpleframework.module.druid.web;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Properties;

import net.simpleframework.common.ClassUtils;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.template.struct.TabButton;
import net.simpleframework.mvc.template.struct.TabButtons;
import net.simpleframework.mvc.template.t1.ResizedTemplatePage;

/**
 * 这是一个开源的软件，请在LGPLv3下合法使用、修改或重新发布。
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
 *         http://www.simpleframework.net
 */
public abstract class AbstractMonitorPage extends ResizedTemplatePage {

	protected static Properties properties;
	static {
		try {
			properties = new Properties();
			properties.load(ClassUtils.getResourceAsStream(DataSourceMonitorPage.class,
					"stat.properties"));
		} catch (final IOException e) {
		}
	}

	@Override
	protected void onInit(final PageParameter pParameter) {
		super.onInit(pParameter);

		addImportCSS(new String[] { getCssResourceHomePath(pParameter) + "/druid.css" });
	}

	@Override
	protected TabButtons getTabButtons(final PageParameter pParameter) {
		return TabButtons.of(new TabButton($m("AbstractMonitorPage.0"),
				uriFor(DataSourceMonitorPage.class)), new TabButton($m("AbstractMonitorPage.1"),
				uriFor(ConnectionMonitorPage.class)), new TabButton($m("AbstractMonitorPage.2"),
				uriFor(SqlMonitorPage.class)));
	}
}
