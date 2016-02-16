/**
* Copyright (c) 2000-present Liferay, Inc. All rights reserved.
*
* This library is free software; you can redistribute it and/or modify it under
* the terms of the GNU Lesser General Public License as published by the Free
* Software Foundation; either version 2.1 of the License, or (at your option)
* any later version.
*
* This library is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
* FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
* details.
*/
import UIKit


public class SaveUserInteractor: ServerConnectorInteractor {

	public var resultUserAttributes: [String:AnyObject]?

	override public func createConnector() -> LiferayUpdateCurrentUserConnector {
		let screenlet = self.screenlet as! SignUpScreenlet

		return LiferayServerContext.connectorFactory.createUpdateCurrentUserConnector(
			viewModel: screenlet.viewModel)
	}

	override public func completedConnector(op: ServerConnector) {
		self.resultUserAttributes = (op as! LiferayUpdateCurrentUserConnector).resultUserAttributes
	}

}
