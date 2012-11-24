package net.simpleframework.module.druid.web;

import static net.simpleframework.common.I18n.$m;
import net.simpleframework.ctx.AbstractModuleContext;
import net.simpleframework.ctx.Module;
import net.simpleframework.mvc.ctx.WebModuleFunction;

public class DruidWebContext extends AbstractModuleContext {

	@Override
	protected Module createModule() {
		return new Module()
				.setName("simple-module-druid")
				.setText("Druid")
				.setDefaultFunction(
						new WebModuleFunction(DataSourceMonitorPage.class).setName(
								"simple-module-druid-DataSourceMonitorPage").setText(
								$m("DruidWebContext.0")));
	}
}
