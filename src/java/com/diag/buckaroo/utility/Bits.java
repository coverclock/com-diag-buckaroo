/**
 * Copyright 2007 Digital Aggregates Corp., Arvada CO 80001-0597, USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Name$
 *
 * $Id$
 */
package com.diag.buckaroo.utility;

/**
 * This class provides some handy little utilities for dealing with bits.
 *
 * @author <A HREF="mailto:coverclock@diag.com">Chip Overclock</A>
 *
 * @version $Revision$
 */
public class Bits {
	
	/**
	 * Returns the bit position of the most significant bit in a byte. The bit
	 * position is the argument to a logical shift operator to generate a mask
	 * with the same bit set. For example, returning a 4 means the mask of the
	 * most significant bit would be (1<<4).
	 * @param datum is the byte.
	 * @return a value in the range 0 to 7, or 8 if there are no bits set.
	 */
	static int msb(byte datum) {
		int rc;
		if ((datum & 0xff) != 0) {
			if ((datum & 0xf0) != 0) {
				if ((datum & 0xc0) != 0) {
					if ((datum & 0x80) != 0) {
						rc = 7;
					} else {
						rc = 6;
					}
				} else {
					if ((datum & 0x20) != 0) {
						rc = 5;
					} else {
						rc = 4;
					}
				}
			} else {
				if ((datum & 0x0c) != 0) {
					if ((datum & 0x08) != 0) {
						rc = 3;
					} else {
						rc = 2;
					}
				} else {
					if ((datum & 0x02) != 0) {
						rc = 1;
					} else {
						rc = 0;
					}
				}
			}
		} else {
			rc = 8;
		}
		return rc;
	}
	
	/**
	 * Returns the bit position of the most significant bit in a short. The bit
	 * position is the argument to a logical shift operator to generate a mask
	 * with the same bit set. For example, returning a 4 means the mask of the
	 * most significant bit would be (1<<4).
	 * @param datum is the short.
	 * @return a value in the range 0 to 15, or 16 if there are no bits set.
	 */
	static int msb(short datum) {
		int rc;
		if ((datum & 0xffff) != 0) {
			if ((datum & 0xff00) != 0) {
				if ((datum & 0xf000) != 0) {
					if ((datum & 0xc000) != 0) {
						if ((datum & 0x8000) != 0) {
							rc = 15;
						} else {
							rc = 14;
						}
					} else {
						if ((datum & 0x2000) != 0) {
							rc = 13;
						} else {
							rc = 12;
						}
					}
				} else {
					if ((datum & 0x0c00) != 0) {
						if ((datum & 0x0800) != 0) {
							rc = 11;
						} else {
							rc = 10;
						}
					} else {
						if ((datum & 0x0200) != 0) {
							rc = 9;
						} else {
							rc = 8;
						}
					}
				}
			} else {
				if ((datum & 0x00f0) != 0) {
					if ((datum & 0x00c0) != 0) {
						if ((datum & 0x0080) != 0) {
							rc = 7;
						} else {
							rc = 6;
						}
					} else {
						if ((datum & 0x0020) != 0) {
							rc = 5;
						} else {
							rc = 4;
						}
					}
				} else {
					if ((datum & 0x000c) != 0) {
						if ((datum & 0x0008) != 0) {
							rc = 3;
						} else {
							rc = 2;
						}
					} else {
						if ((datum & 0x0002) != 0) {
							rc = 1;
						} else {
							rc = 0;
						}
					}
				}
			}
		} else {
			rc = 16;
		}
		return rc;
	}
	
	/**
	 * Returns the bit position of the most significant bit in an int. The bit
	 * position is the argument to a logical shift operator to generate a mask
	 * with the same bit set. For example, returning a 4 means the mask of the
	 * most significant bit would be (1<<4).
	 * @param datum is the int.
	 * @return a value in the range 0 to 31, or 32 if there are no bits set.
	 */
	static int msb(int datum) {
		int rc;
		if ((datum & 0xffffffff) != 0) {
			if ((datum & 0xffff0000) != 0) {
				if ((datum & 0xff000000) != 0) {
					if ((datum & 0xf0000000) != 0) {
						if ((datum & 0xc0000000) != 0) {
							if ((datum & 0x80000000) != 0) {
								rc = 31;
							} else {
								rc = 30;
							}
						} else {
							if ((datum & 0x20000000) != 0) {
								rc = 29;
							} else {
								rc = 28;
							}
						}
					} else {
						if ((datum & 0x0c000000) != 0) {
							if ((datum & 0x08000000) != 0) {
								rc = 27;
							} else {
								rc = 26;
							}
						} else {
							if ((datum & 0x02000000) != 0) {
								rc = 25;
							} else {
								rc = 24;
							}
						}
					}
				} else {
					if ((datum & 0x00f00000) != 0) {
						if ((datum & 0x00c00000) != 0) {
							if ((datum & 0x00800000) != 0) {
								rc = 23;
							} else {
								rc = 22;
							}
						} else {
							if ((datum & 0x00200000) != 0) {
								rc = 21;
							} else {
								rc = 20;
							}
						}
					} else {
						if ((datum & 0x000c0000) != 0) {
							if ((datum & 0x00080000) != 0) {
								rc = 19;
							} else {
								rc = 18;
							}
						} else {
							if ((datum & 0x00020000) != 0) {
								rc = 17;
							} else {
								rc = 16;
							}
						}
					}
				}
			} else {
				if ((datum & 0x0000ff00) != 0) {
					if ((datum & 0x0000f000) != 0) {
						if ((datum & 0x0000c000) != 0) {
							if ((datum & 0x00008000) != 0) {
								rc = 15;
							} else {
								rc = 14;
							}
						} else {
							if ((datum & 0x00002000) != 0) {
								rc = 13;
							} else {
								rc = 12;
							}
						}
					} else {
						if ((datum & 0x00000c00) != 0) {
							if ((datum & 0x00000800) != 0) {
								rc = 11;
							} else {
								rc = 10;
							}
						} else {
							if ((datum & 0x00000200) != 0) {
								rc = 9;
							} else {
								rc = 8;
							}
						}
					}
				} else {
					if ((datum & 0x000000f0) != 0) {
						if ((datum & 0x000000c0) != 0) {
							if ((datum & 0x00000080) != 0) {
								rc = 7;
							} else {
								rc = 6;
							}
						} else {
							if ((datum & 0x00000020) != 0) {
								rc = 5;
							} else {
								rc = 4;
							}
						}
					} else {
						if ((datum & 0x0000000c) != 0) {
							if ((datum & 0x00000008) != 0) {
								rc = 3;
							} else {
								rc = 2;
							}
						} else {
							if ((datum & 0x00000002) != 0) {
								rc = 1;
							} else {
								rc = 0;
							}
						}
					}
				}
			}
		} else {
			rc = 32;
		}
		return rc;
	}

}
