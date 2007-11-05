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
	
	static byte msb(byte word) {
		byte rc;
		if ((word & 0xff) != 0) {
			if ((word & 0xf0) != 0) {
				if ((word & 0xc0) != 0) {
					if ((word & 0x80) != 0) {
						rc = (byte)0x80;
					} else {
						rc = (byte)0x40;
					}
				} else {
					if ((word & 0x20) != 0) {
						rc = (byte)0x20;
					} else {
						rc = (byte)0x10;
					}
				}
			} else {
				if ((word & 0x0c) != 0) {
					if ((word & 0x08) != 0) {
						rc = (byte)0x08;
					} else {
						rc = (byte)0x04;
					}
				} else {
					if ((word & 0x02) != 0) {
						rc = (byte)0x02;
					} else {
						rc = (byte)0x01;
					}
				}
			}
		} else {
			rc = (byte)0x00;
		}
		return rc;
	}
	
	static short msb(short word) {
		short rc;
		if ((word & 0xffff) != 0) {
			if ((word & 0xff00) != 0) {
				if ((word & 0xf000) != 0) {
					if ((word & 0xc000) != 0) {
						if ((word & 0x8000) != 0) {
							rc = (short)0x8000;
						} else {
							rc = (short)0x4000;
						}
					} else {
						if ((word & 0x2000) != 0) {
							rc = (short)0x2000;
						} else {
							rc = (short)0x1000;
						}
					}
				} else {
					if ((word & 0x0c00) != 0) {
						if ((word & 0x0800) != 0) {
							rc = (short)0x0800;
						} else {
							rc = (short)0x0400;
						}
					} else {
						if ((word & 0x0200) != 0) {
							rc = (short)0x0200;
						} else {
							rc = (short)0x0100;
						}
					}
				}
			} else {
				if ((word & 0x00f0) != 0) {
					if ((word & 0x00c0) != 0) {
						if ((word & 0x0080) != 0) {
							rc = (short)0x0080;
						} else {
							rc = (short)0x0040;
						}
					} else {
						if ((word & 0x0020) != 0) {
							rc = (short)0x0020;
						} else {
							rc = (short)0x0010;
						}
					}
				} else {
					if ((word & 0x000c) != 0) {
						if ((word & 0x0008) != 0) {
							rc = (short)0x0008;
						} else {
							rc = (short)0x0004;
						}
					} else {
						if ((word & 0x0002) != 0) {
							rc = (short)0x0002;
						} else {
							rc = (short)0x0001;
						}
					}
				}
			}
		} else {
			rc = (short)0x0000;
		}
		return rc;
	}
	
	static int msb(int word) {
		int rc = 0;
		if ((word & 0xffffffff) != 0) {
			if ((word & 0xffff0000) != 0) {
				if ((word & 0xff000000) != 0) {
					if ((word & 0xf0000000) != 0) {
						if ((word & 0xc0000000) != 0) {
							if ((word & 0x80000000) != 0) {
								rc = (int)0x80000000;
							} else {
								rc = (int)0x40000000;
							}
						} else {
							if ((word & 0x20000000) != 0) {
								rc = (int)0x20000000;
							} else {
								rc = (int)0x10000000;
							}
						}
					} else {
						if ((word & 0x0c000000) != 0) {
							if ((word & 0x08000000) != 0) {
								rc = (int)0x08000000;
							} else {
								rc = (int)0x04000000;
							}
						} else {
							if ((word & 0x02000000) != 0) {
								rc = (int)0x02000000;
							} else {
								rc = (int)0x01000000;
							}
						}
					}
				} else {
					if ((word & 0x00f00000) != 0) {
						if ((word & 0x00c00000) != 0) {
							if ((word & 0x00800000) != 0) {
								rc = (int)0x00800000;
							} else {
								rc = (int)0x00400000;
							}
						} else {
							if ((word & 0x00200000) != 0) {
								rc = (int)0x00200000;
							} else {
								rc = (int)0x00100000;
							}
						}
					} else {
						if ((word & 0x000c0000) != 0) {
							if ((word & 0x00080000) != 0) {
								rc = (int)0x00080000;
							} else {
								rc = (int)0x00040000;
							}
						} else {
							if ((word & 0x00020000) != 0) {
								rc = (int)0x00020000;
							} else {
								rc = (int)0x00010000;
							}
						}
					}
				}
			} else {
				if ((word & 0x0000ff00) != 0) {
					if ((word & 0x0000f000) != 0) {
						if ((word & 0x0000c000) != 0) {
							if ((word & 0x00008000) != 0) {
								rc = (int)0x00008000;
							} else {
								rc = (int)0x00004000;
							}
						} else {
							if ((word & 0x00002000) != 0) {
								rc = (int)0x00002000;
							} else {
								rc = (int)0x00001000;
							}
						}
					} else {
						if ((word & 0x00000c00) != 0) {
							if ((word & 0x00000800) != 0) {
								rc = (int)0x00000800;
							} else {
								rc = (int)0x00000400;
							}
						} else {
							if ((word & 0x00000200) != 0) {
								rc = (int)0x00000200;
							} else {
								rc = (int)0x00000100;
							}
						}
					}
				} else {
					if ((word & 0x000000f0) != 0) {
						if ((word & 0x000000c0) != 0) {
							if ((word & 0x00000080) != 0) {
								rc = (int)0x00000080;
							} else {
								rc = (int)0x00000040;
							}
						} else {
							if ((word & 0x00000020) != 0) {
								rc = (int)0x00000020;
							} else {
								rc = (int)0x00000010;
							}
						}
					} else {
						if ((word & 0x0000000c) != 0) {
							if ((word & 0x00000008) != 0) {
								rc = (int)0x00000008;
							} else {
								rc = (int)0x00000004;
							}
						} else {
							if ((word & 0x00000002) != 0) {
								rc = (int)0x00000002;
							} else {
								rc = (int)0x00000001;
							}
						}
					}
				}
			}
		} else {
			rc = (int)0x00000000;
		}
		return rc;
	}

}
