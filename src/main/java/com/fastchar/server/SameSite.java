package com.fastchar.server;

public enum SameSite {

		/**
		 * Cookies are sent in both first-party and cross-origin requests.
		 */
		NONE("None"),

		/**
		 * Cookies are sent in a first-party context, also when following a link to the
		 * origin site.
		 */
		LAX("Lax"),

		/**
		 * Cookies are only sent in a first-party context (i.e. not when following a link
		 * to the origin site).
		 */
		STRICT("Strict");

		private final String attributeValue;

		SameSite(String attributeValue) {
			this.attributeValue = attributeValue;
		}

		public String attributeValue() {
			return this.attributeValue;
		}

	}