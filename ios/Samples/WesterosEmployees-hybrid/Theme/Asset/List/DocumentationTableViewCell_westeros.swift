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

open class DocumentationTableViewCell_westeros: UITableViewCell {

	@IBOutlet fileprivate weak var docImage: UIImageView?
	@IBOutlet fileprivate weak var docTitle: UILabel?
	@IBOutlet fileprivate weak var docDescription: UILabel?

	open var title: String? {
		get {
			return docTitle?.text
		}
		set {
			docTitle?.text = newValue
		}
	}

	open var fileDescription: String? {
		get {
			return docDescription?.text
		}
		set {
			docDescription?.text = newValue
		}
	}

	open var fileExtension: String {
		get {
			return ""
		}
		set {
			switch newValue {
			case "pdf":
				docImage?.image = UIImage(named: "file-pdf")
			case "mp3":
				docImage?.image = UIImage(named: "file-music")
			case "mp4":
				docImage?.image = UIImage(named: "file-video")
			case "png", "jpg", "jpeg":
				docImage?.image = UIImage(named: "file-photo")
			default:
				docImage?.image = UIImage(named: "file-none")
			}
		}
	}
}
